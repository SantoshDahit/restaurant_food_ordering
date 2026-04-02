package com.restaurant.api.dto;

import com.restaurant.api.constant.FileType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

public class FileDto {

    public record CreateRequest(
            @NotNull FileType type,
            String url
    ) {}

    @Getter
    public static class Response {
        private String code;
        private FileType type;
        private String url;
        private Boolean isSuccess;
        private LocalDateTime createdAt;
    }
}
