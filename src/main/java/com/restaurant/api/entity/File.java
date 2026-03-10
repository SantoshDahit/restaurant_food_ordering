package com.restaurant.api.entity;

import com.restaurant.api.constant.FileType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "file")
public class File {

    @Id
    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private FileType type;

    @Column(name = "url")
    private String url;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess = false;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    public File(String url, FileType type) {
        this.code = UUID.randomUUID().toString();
        this.type = type;
        this.url = url;
        this.isSuccess = true;
    }

    public void markSuccess(String url) {
        this.url = url;
        this.isSuccess = true;
        this.updateAt = LocalDateTime.now();
    }
}
