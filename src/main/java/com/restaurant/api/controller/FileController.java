package com.restaurant.api.controller;

import com.restaurant.api.dto.FileDto;
import com.restaurant.api.entity.FileEntity;
import com.restaurant.api.mapper.FileMapper;
import com.restaurant.api.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;
    private final FileMapper fileMapper;

    @PostMapping("/upload")
    public FileDto.Response upload(@RequestParam("file") MultipartFile file) {
        FileEntity entity = fileService.upload(file);
        return fileMapper.toResponse(entity);
    }

    @GetMapping("/{code}")
    public FileDto.Response getByCode(@PathVariable String code) {
        FileEntity entity = fileService.getByCode(code);
        return fileMapper.toResponse(entity);
    }
}
