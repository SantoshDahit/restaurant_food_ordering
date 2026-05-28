package com.restaurant.api.controller;

import com.restaurant.api.dto.ReceiptDto;
import com.restaurant.api.service.facade.ReceiptFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/receipts")
@RequiredArgsConstructor
public class ReceiptController {
    private final ReceiptFacade receiptFacade;

    @GetMapping("/{code}")
    public ReceiptDto.Response getByCode(@PathVariable String code) {
        return receiptFacade.getByCode(code);
    }

    @GetMapping("/by-order/{orderCode}")
    public ReceiptDto.Response getByOrderCode(@PathVariable String orderCode) {
        return receiptFacade.getByOrderCode(orderCode);
    }
}
