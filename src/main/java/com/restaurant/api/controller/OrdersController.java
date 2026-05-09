package com.restaurant.api.controller;

import com.restaurant.api.dto.OrdersDto;
import com.restaurant.api.service.facade.OrdersFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrdersController {
    private final OrdersFacade ordersFacade;

    @PostMapping
    public OrdersDto.Response create(@Valid @RequestBody OrdersDto.CreateRequest request) {
        return ordersFacade.create(request);
    }

    @GetMapping("/{code}")
    public OrdersDto.Response getByCode(@PathVariable String code) {
        return ordersFacade.getByCode(code);
    }

    @GetMapping("/{code}/detail")
    public OrdersDto.DetailResponse getDetail(@PathVariable String code) {
        return ordersFacade.getDetail(code);
    }

    @GetMapping("/search")
    public Page<OrdersDto.Response> search(@ModelAttribute OrdersDto.SearchRequest request,
                                           Pageable pageable) {
        return ordersFacade.search(request, pageable);
    }

    @PatchMapping("/{code}/status")
    public OrdersDto.Response updateStatus(@PathVariable String code,
                                           @RequestBody OrdersDto.StatusUpdateRequest request) {
        return ordersFacade.updateStatus(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        ordersFacade.delete(code);
    }
}
