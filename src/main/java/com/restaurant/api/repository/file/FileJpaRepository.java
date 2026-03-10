package com.restaurant.api.repository.file;

import com.restaurant.api.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileJpaRepository extends JpaRepository<File, String> {
}
