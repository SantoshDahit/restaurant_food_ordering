package com.restaurant.api.repository.file;

import com.restaurant.api.entity.FileEntity;
import java.util.Optional;

public interface FileRepository {
    Optional<FileEntity> findByCode(String code);
    FileEntity save(FileEntity fileEntity);
}
