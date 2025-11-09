package com.example.demo.request;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.example.demo.model.FilterCriteria;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
public class DefaultRequest {
    private Pageable pageable;
    private List<FilterCriteria> filters;
}
