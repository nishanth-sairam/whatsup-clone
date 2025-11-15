package com.example.demo.resolver;

import com.example.demo.annotation.PageReq;
import com.example.demo.constant.CommonConstant;
import com.example.demo.model.FilterCriteria;
import com.example.demo.model.FilterOperator;
import com.example.demo.request.DefaultRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PageAndFilterResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper mapper;

    // Field cache to improve reflection performance
    private final Map<String, Field> fieldCache = new ConcurrentHashMap<>();

    @Override
    @Nullable
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mvc,
                                  @NonNull NativeWebRequest request, @Nullable WebDataBinderFactory factory) throws Exception {

        Class<?> clazz = parameter.getParameterType();
        Object requestObj = instantiateRequestObject(clazz);
        HttpServletRequest servletRequest = (HttpServletRequest) request.getNativeRequest();
        if (servletRequest != null) {
            bindRequestBody(servletRequest, clazz, requestObj);
            bindAuthenticatedUser(servletRequest, requestObj);
        }
        bindPathVariables(request, requestObj);
        bindQueryParameters(request, requestObj);
        bindPagination(request, requestObj);
        bindFilters(request, requestObj);
        bindAuthentication(requestObj);
        return requestObj;
    }

    private Object instantiateRequestObject(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Failed to instantiate parameter type: {}", clazz.getName(), e);
            throw new IllegalArgumentException("Unable to instantiate request object of type: " + clazz.getName(), e);
        }
    }

    private void bindRequestBody(HttpServletRequest servletRequest, Class<?> clazz, Object requestObj) {
        if (!hasRequestBody(servletRequest)) {
            return;
        }

        try {
            Object body = mapper.readValue(servletRequest.getInputStream(), clazz);
            BeanUtils.copyProperties(body, requestObj);
            log.debug("Bound request body to {}", clazz.getSimpleName());
        } catch (Exception e) {
            log.warn("Failed to parse request body for type: {}", clazz.getName(), e);
            // Continue with other bindings even if body parsing fails
        }
    }

    private void bindAuthenticatedUser(HttpServletRequest servletRequest, Object requestObj) {
        Object authenticatedUser = servletRequest.getAttribute(CommonConstant.AUTHENTICATED_USER);
        if (authenticatedUser != null) {
            setFieldIfExists(requestObj, "user", authenticatedUser);
            log.debug("Bound authenticated user from request attribute");
        }
    }

    private void bindPathVariables(NativeWebRequest request, Object requestObj) {
        @SuppressWarnings("unchecked")
        Map<String, String> pathVars = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);

        if (pathVars != null && !pathVars.isEmpty()) {
            pathVars.forEach((k, v) -> setFieldIfExists(requestObj, k, v));
            log.debug("Bound {} path variables", pathVars.size());
        }
    }

    private void bindQueryParameters(NativeWebRequest request, Object requestObj) {
        Map<String, String[]> params = request.getParameterMap();
        if (params == null || params.isEmpty()) {
            return;
        }

        int boundCount = 0;
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0 && values[0] != null) {
                setFieldIfExists(requestObj, entry.getKey(), values[0]);
                boundCount++;
            }
        }

        if (boundCount > 0) {
            log.debug("Bound {} query parameters", boundCount);
        }
    }

    private void bindPagination(NativeWebRequest request, Object requestObj) {
        Pageable pageable = createPageable(request);
        if (pageable != null) {
            setFieldIfExists(requestObj, CommonConstant.PARAM_PAGEABLE, pageable);
            log.debug("Created pageable: page={}, size={}, sort={}",
                    pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        }
    }

    private void bindFilters(NativeWebRequest request, Object requestObj) {
        List<FilterCriteria> filters = parseFilters(request);
        if (filters != null && !filters.isEmpty()) {
            setFieldIfExists(requestObj, CommonConstant.PARAM_FILTERS, filters);
            log.debug("Parsed {} filter criteria", filters.size());
        }
    }

    private void bindAuthentication(Object requestObj) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            setFieldIfExists(requestObj, "authentication", authentication);
            log.debug("Bound authentication for user: {}", authentication.getName());
        }
    }

    private boolean hasRequestBody(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        int contentLength = request.getContentLength();
        String contentType = request.getContentType();

        return contentLength > 0 && contentType != null &&
                (contentType.contains("application/json") || contentType.contains("application/xml"));
    }

    private void setFieldIfExists(Object target, String fieldName, Object value) {
        if (target == null || fieldName == null || value == null) {
            return;
        }

        try {
            String cacheKey = target.getClass().getName() + "." + fieldName;
            Field field = fieldCache.computeIfAbsent(cacheKey,
                    k -> ReflectionUtils.findField(target.getClass(), fieldName));

            if (field != null) {
                field.setAccessible(true);
                Object convertedValue = convert(value, field.getType());
                field.set(target, convertedValue);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Type conversion failed for field '{}' with value '{}': {}",
                    fieldName, value, e.getMessage());
        } catch (Exception e) {
            log.debug("Could not set field '{}' on {}: {}",
                    fieldName, target.getClass().getSimpleName(), e.getMessage());
        }
    }

    private Object convert(Object value, Class<?> type) {
        if (value == null || type == null) {
            return null;
        }

        if (type.isAssignableFrom(value.getClass())) {
            return value;
        }

        try {
            return mapper.convertValue(value, type);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to convert value '{}' to type {}", value, type.getSimpleName());
            throw e;
        }
    }

    private Pageable createPageable(NativeWebRequest request) {
        if (request == null) {
            return null;
        }

        int page = parseInt(request.getParameter(CommonConstant.PARAM_PAGE), CommonConstant.DEFAULT_PAGE);
        int size = parseInt(request.getParameter(CommonConstant.PARAM_SIZE), CommonConstant.DEFAULT_SIZE);

        // Validate and cap the size
        if (size <= 0) {
            size = CommonConstant.DEFAULT_SIZE;
            log.debug("Invalid page size, using default: {}", CommonConstant.DEFAULT_SIZE);
        } else if (size > CommonConstant.MAX_SIZE) {
            size = CommonConstant.MAX_SIZE;
            log.debug("Page size exceeds maximum, capping at: {}", CommonConstant.MAX_SIZE);
        }

        // Validate page number
        if (page < 0) {
            page = CommonConstant.DEFAULT_PAGE;
            log.debug("Invalid page number, using default: {}", CommonConstant.DEFAULT_PAGE);
        }

        String sortBy = Optional.ofNullable(request.getParameter(CommonConstant.PARAM_SORT_BY))
                .filter(s -> !s.trim().isEmpty())
                .orElse(CommonConstant.DEFAULT_SORT_FIELD);
        String dir = Optional.ofNullable(request.getParameter(CommonConstant.PARAM_DIR))
                .filter(s -> !s.trim().isEmpty())
                .orElse(CommonConstant.DEFAULT_SORT_DIRECTION);

        Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Support multiple sort fields separated by comma
        Sort sort = createSort(sortBy, direction);

        return PageRequest.of(page, size, sort);
    }

    private Sort createSort(String sortBy, Sort.Direction direction) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return Sort.by(direction, CommonConstant.DEFAULT_SORT_FIELD);
        }

        String[] fields = sortBy.split(CommonConstant.SORT_DELIMITER);
        List<Sort.Order> orders = new ArrayList<>();

        for (String field : fields) {
            String trimmedField = field.trim();
            if (!trimmedField.isEmpty()) {
                orders.add(new Sort.Order(direction, trimmedField));
            }
        }

        return orders.isEmpty() ? Sort.by(direction, CommonConstant.DEFAULT_SORT_FIELD) : Sort.by(orders);
    }

    private List<FilterCriteria> parseFilters(NativeWebRequest request) {
        if (request == null) {
            return new ArrayList<>();
        }

        List<FilterCriteria> filters = new ArrayList<>();
        Map<String, String[]> params = request.getParameterMap();

        if (params == null || params.isEmpty()) {
            return filters;
        }

        params.forEach((key, values) -> {
            if (key != null && key.startsWith(CommonConstant.FILTER_PREFIX) && values != null && values.length > 0) {
                try {
                    FilterCriteria filter = parseFilterParameter(key, values[0]);
                    if (filter != null) {
                        filters.add(filter);
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid filter parameter '{}': {}", key, e.getMessage());
                } catch (Exception e) {
                    log.error("Error parsing filter parameter '{}'", key, e);
                }
            }
        });

        return filters;
    }

    private FilterCriteria parseFilterParameter(String key, String value) {
        if (key == null || value == null) {
            return null;
        }

        // Remove "filter." prefix
        String[] parts = key.split(CommonConstant.FIELD_DELIMITER, 2);
        if (parts.length < 2) {
            log.debug("Invalid filter format, missing field name: {}", key);
            return null;
        }

        // Split field and operator
        String[] fieldOp = parts[1].split(CommonConstant.OPERATOR_DELIMITER, 2);
        String field = fieldOp[0];

        if (field == null || field.trim().isEmpty()) {
            log.debug("Invalid filter format, empty field name: {}", key);
            return null;
        }

        String operator = fieldOp.length > 1 ? fieldOp[1] : CommonConstant.DEFAULT_OPERATOR;

        try {
            return FilterCriteria.builder()
                    .field(field.trim())
                    .operator(FilterOperator.from(operator))
                    .value(value)
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid operator '{}' in filter parameter '{}'", operator, key);
            throw e;
        }
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PageReq.class)
                || DefaultRequest.class.isAssignableFrom(parameter.getParameterType());
    }

    private int parseInt(String val, int def) {
        if (val == null || val.trim().isEmpty()) {
            return def;
        }

        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            log.debug("Failed to parse integer from '{}', using default: {}", val, def);
            return def;
        }
    }
}