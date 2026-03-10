//package com.restaurant.api.service;
//
//import com.restaurant.api.common.UuidUtil;
//import com.restaurant.api.constant.FileType;
//import com.restaurant.api.dto.FileDto;
//import com.restaurant.api.entity.File;
//import com.restaurant.api.exception.ApiException;
//import com.restaurant.api.exception.ErrorCode;
//import com.restaurant.api.repository.file.FileRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class FileService {
//    private final FileRepository fileRepository;
//
//    @Value("${file.upload-dir}")
//    private String uploadDir;
//
//    @Value("${file.base-url}")
//    private String baseUrl;
//
//    private static final List<String> ALLOWED_TYPES = List.of(
//            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
//    );
//
//    @Transactional(readOnly = true)
//    public File getByCode(String code) {
//        return fileRepository.findByCode(code)
//                .orElseThrow(() -> new ApiException(ErrorCode.FILE_NOT_FOUND));
//    }
//
//    @Transactional
//    public File create(FileDto.CreateRequest request) {
//        File file = new File();
//        return fileRepository.save(file);
//    }
//
//    @Transactional
//    public File upload(MultipartFile file) {
//        if (file.isEmpty()) {
//            throw new ApiException(ErrorCode.FILE_NOT_FOUND);
//        }
//        String contentType = file.getContentType();
//        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
//            throw new IllegalArgumentException("Only image files are allowed (jpg, png, gif, webp)");
//        }
//        try {
//            Path uploadPath = Paths.get(uploadDir);
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//            String original = file.getOriginalFilename();
//            String ext = (original != null && original.contains("."))
//                    ? original.substring(original.lastIndexOf("."))
//                    : ".jpg";
//            String filename = UuidUtil.generate() + ext;
//            Files.copy(file.getInputStream(), uploadPath.resolve(filename));
//
//            String url = baseUrl + "/" + filename;
//            File entity = new File();
//            return fileRepository.save(entity);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to store file", e);
//        }
//    }
//}
