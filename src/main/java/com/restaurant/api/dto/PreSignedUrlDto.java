package com.restaurant.api.dto;


import com.restaurant.api.constant.FileType;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

public record PreSignedUrlDto() {

    public record Request(
            @Pattern(regexp = "^[^\\\\/:*?\"<>|]+\\.(jpg|jpeg|png|gif|bmp)$", message = "Invalid file name format")
            String fileName,
            String folderName,
            FileType type
    ){}

    @Builder
    public record Response(
            String id,
            String url,
            String preSignedUrl
    ){}
}
