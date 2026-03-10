package com.restaurant.api.repository.file;

import com.restaurant.api.entity.File;
import java.util.Optional;

public interface FileRepository {
    Optional<File> findByCode(String code);
    File save(File file);
}
