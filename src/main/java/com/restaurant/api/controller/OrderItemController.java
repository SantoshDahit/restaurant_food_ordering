package com.restaurant.api.controller;

import com.restaurant.api.dto.OrderItemDto;
import com.restaurant.api.service.facade.OrderItemFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderItemController {
    private final OrderItemFacade orderItemFacade;

    @PostMapping("/{orderCode}/items")
    public OrderItemDto.Response addItem(@PathVariable String orderCode,
                                         @Valid @RequestBody OrderItemDto.CreateRequest request) {
        return orderItemFacade.addItem(orderCode, request);
    }

    @PatchMapping("/{orderCode}/items/{code}")
    public OrderItemDto.Response updateItem(@PathVariable String orderCode,
                                            @PathVariable String code,
                                            @RequestBody OrderItemDto.PatchRequest request) {
        return orderItemFacade.updateItem(orderCode, code, request);
    }

    @DeleteMapping("/{orderCode}/items/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable String orderCode,
                           @PathVariable String code) {
        orderItemFacade.removeItem(orderCode, code);
    }

    @GetMapping("/{orderCode}/items")
    public List<OrderItemDto.Response> getItemsByOrder(@PathVariable String orderCode) {
        return orderItemFacade.getItemsByOrder(orderCode);
    }
}
