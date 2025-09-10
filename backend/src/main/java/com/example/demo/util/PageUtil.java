package com.example.demo.util;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtil {

    public static Pageable getPageable(
            Integer page,
            Integer size,
            String sortBy,
            String sortDir,
            String defaultSortBy,
            String defaultSortDir
    ) {
        int pageNumber = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 5;
        String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : defaultSortBy;
        String sortDirection = (sortDir != null && !sortDir.isEmpty()) ? sortDir : defaultSortDir;

        Sort sort = sortDirection.equalsIgnoreCase("asc") ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        return PageRequest.of(pageNumber, pageSize, sort);
    }
}