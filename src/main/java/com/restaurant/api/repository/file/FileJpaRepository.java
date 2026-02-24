package com.restaurant.api.repository.file;

import com.restaurant.api.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileJpaRepository extends JpaRepository<FileEntity, String> {
}
