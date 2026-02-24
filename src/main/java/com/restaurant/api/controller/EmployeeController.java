package com.restaurant.api.controller;

import com.restaurant.api.dto.EmployeeDto;
import com.restaurant.api.service.facade.EmployeeFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeFacade employeeFacade;

    @PostMapping
    public EmployeeDto.Response create(@Valid @RequestBody EmployeeDto.CreateRequest request) {
        return employeeFacade.create(request);
    }

    @GetMapping("/{code}")
    public EmployeeDto.Response getByCode(@PathVariable String code) {
        return employeeFacade.getByCode(code);
    }

    @GetMapping("/search")
    public Page<EmployeeDto.Response> search(@ModelAttribute EmployeeDto.SearchRequest request,
                                             Pageable pageable) {
        return employeeFacade.search(request, pageable);
    }

    @PatchMapping("/{code}")
    public EmployeeDto.Response update(@PathVariable String code,
                                       @RequestBody EmployeeDto.PatchRequest request) {
        return employeeFacade.update(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        employeeFacade.delete(code);
    }
}
