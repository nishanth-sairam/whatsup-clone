package com.example.demo.specification;

import com.example.demo.model.FilterCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericSpecification {

    public static <T> Specification<T> withFilters(List<FilterCriteria> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (FilterCriteria filter : filters) {
                predicates.add(buildPredicate(filter, root, criteriaBuilder));
            }

            // Return combined predicates with AND logic
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static <T> Predicate buildPredicate(FilterCriteria filter, Root<T> root, CriteriaBuilder criteriaBuilder) {
        Path<?> path = getPath(root, filter.getField());
        Class<?> fieldType = path.getJavaType();
        Object value = convertValueToFieldType(filter.getValue(), fieldType);

        switch (filter.getOperator()) {
            case eq:
                return criteriaBuilder.equal(path, value);
            case ne:
                return criteriaBuilder.notEqual(path, value);
            case like:
                return criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
            case not_like:
                return criteriaBuilder.notLike(criteriaBuilder.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
            case gt:
                return criteriaBuilder.greaterThan(path.as(Comparable.class), (Comparable) value);
            case lt:
                return criteriaBuilder.lessThan(path.as(Comparable.class), (Comparable) value);
            case gte:
                return criteriaBuilder.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case lte:
                return criteriaBuilder.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case in:
                return path.in(parseList(fieldType, filter.getValue()));
            case not_in:
                return criteriaBuilder.not(path.in(parseList(fieldType, filter.getValue())));
            case between:
                String[] range = filter.getValue().toString().split(",");
                if (range.length == 2) {
                    return criteriaBuilder.between(path.as(Comparable.class),
                            (Comparable) convertValueToFieldType(range[0].trim(), fieldType),
                            (Comparable) convertValueToFieldType(range[1].trim(), fieldType));
                }
                throw new IllegalArgumentException("Between operator requires exactly 2 values separated by comma");
            case is_null:
                return criteriaBuilder.isNull(path);
            case is_not_null:
                return criteriaBuilder.isNotNull(path);
            default:
                throw new IllegalArgumentException("Unsupported operator: " + filter.getOperator());
        }
    }

    private static Path<?> getPath(Root<?> root, String field) {
        String[] parts = field.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    private static Object convertValueToFieldType(Object value, Class<?> fieldType) {
        if (value == null) {
            return null;
        }

        try {
            switch (fieldType.getSimpleName()) {
                case "String":
                    return value.toString();
                case "Integer":
                case "int":
                    return Integer.valueOf(value.toString());
                case "Long":
                case "long":
                    return Long.valueOf(value.toString());
                case "Double":
                case "double":
                    return Double.valueOf(value.toString());
                case "Boolean":
                case "boolean":
                    return Boolean.valueOf(value.toString());
                case "UUID":
                    return java.util.UUID.fromString(value.toString());
                default:
                    return value;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert value '" + value + "' to type " + fieldType.getSimpleName(), e);
        }
    }

    private static List<?> parseList(Class<?> type, Object value) {
        return Arrays.stream(value.toString().split(","))
                .map(v -> convertValueToFieldType(v.trim(), type))
                .toList();
    }
}