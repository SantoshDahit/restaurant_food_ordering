package com.restaurant.api.controller;

import com.restaurant.api.dto.AttendanceDto;
import com.restaurant.api.service.facade.AttendanceFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceFacade attendanceFacade;

    @PostMapping
    public AttendanceDto.Response create(@Valid @RequestBody AttendanceDto.CreateRequest request) {
        return attendanceFacade.create(request);
    }

    @GetMapping("/search")
    public Page<AttendanceDto.Response> search(@ModelAttribute AttendanceDto.SearchRequest request,
                                               Pageable pageable) {
        return attendanceFacade.search(request, pageable);
    }

    @PatchMapping("/{code}")
    public AttendanceDto.Response update(@PathVariable String code,
                                         @RequestBody AttendanceDto.PatchRequest request) {
        return attendanceFacade.update(code, request);
    }
}
