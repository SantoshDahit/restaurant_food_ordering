package com.restaurant.api.controller;

import com.restaurant.api.constant.FileType;
import com.restaurant.api.dto.FileDto;
import com.restaurant.api.dto.PreSignedUrlDto;
import com.restaurant.api.entity.File;
import com.restaurant.api.mapper.FileMapper;
import com.restaurant.api.service.FileLocalUploadService;
import com.restaurant.api.service.FilePresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileMapper fileMapper;
    private final FilePresignedUrlService filePresignedUrlService;
    private final FileLocalUploadService fileLocalUploadService;

    @GetMapping("/{code}")
    public FileDto.Response getByCode(@PathVariable String code) {
        File entity = filePresignedUrlService.getById(code);
        return fileMapper.toResponse(entity);
    }

    @PostMapping("/pre-signed-url")
    public List<PreSignedUrlDto.Response> generatePreSignedUrl(@RequestBody List<PreSignedUrlDto.Request> requests) {
        return filePresignedUrlService.generatePreSignedUrls(requests, 15);
    }
}
