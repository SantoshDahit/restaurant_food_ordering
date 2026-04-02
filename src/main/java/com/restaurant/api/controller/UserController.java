package com.restaurant.api.controller;

import com.restaurant.api.dto.UserDto;
import com.restaurant.api.service.facade.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserFacade userFacade;

    @GetMapping("/{code}")
    public UserDto.Response getByCode(@PathVariable String code) {
        return userFacade.getByCode(code);
    }
    @GetMapping("/search")
    public Page<UserDto.Response> search( @ModelAttribute UserDto.SearchRequest searchRequest, Pageable pageable) {
        return userFacade.search(searchRequest, pageable);
    }
}
