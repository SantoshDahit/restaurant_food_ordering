package com.restaurant.api.entity;

import com.restaurant.api.constant.FileType;
import com.restaurant.api.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "file")
@EntityListeners(AuditingEntityListener.class)
public class File extends BaseTimeEntity {

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

    public File(String url, FileType type) {
        this.code = UUID.randomUUID().toString();
        this.type = type;
        this.url = url;
        this.isSuccess = true;
    }

    public void markSuccess(String url) {
        this.url = url;
        this.isSuccess = true;
    }
}
