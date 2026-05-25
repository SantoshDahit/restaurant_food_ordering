package com.restaurant.api.service;

import com.restaurant.api.constant.FileType;
import com.restaurant.api.entity.File;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.file.FileRepository;
import com.restaurant.api.util.FileAttachmentUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileLocalUploadService {

    private final FileRepository fileRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.public-base-url:/api/uploads}")
    private String publicBaseUrl;

    @PostConstruct
    void ensureUploadDir() throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
    }

    @Transactional
    public File store(MultipartFile multipart, String folderName, FileType type) {
        if (multipart == null || multipart.isEmpty()) {
            throw new ApiException(ErrorCode.FILE_NOT_FOUND);
        }
        String safeFolder = (folderName == null || folderName.isBlank()) ? "misc" : folderName;
        String original = multipart.getOriginalFilename() == null ? "file" : multipart.getOriginalFilename();
        String fileName = FileAttachmentUtil.generateUniqueFileNameWithTimeStamp(original);

        Path targetDir = Paths.get(uploadDir, safeFolder);
        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(fileName);
            try (var in = multipart.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to save uploaded file", e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        String publicUrl = publicBaseUrl + "/" + safeFolder + "/" + fileName;
        File file = new File(publicUrl, type);
        return fileRepository.save(file);
    }
}
