package com.restaurant.api.repository.file;

import com.restaurant.api.entity.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {
    private final FileJpaRepository fileJpaRepository;

    @Override
    public Optional<File> findByCode(String code) {
        return fileJpaRepository.findById(code);
    }

    @Override
    public File save(File file) {
        return fileJpaRepository.save(file);
    }
}
