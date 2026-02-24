package com.restaurant.api.repository.file;

import com.restaurant.api.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {
    private final FileJpaRepository fileJpaRepository;

    @Override
    public Optional<FileEntity> findByCode(String code) {
        return fileJpaRepository.findById(code);
    }

    @Override
    public FileEntity save(FileEntity fileEntity) {
        return fileJpaRepository.save(fileEntity);
    }
}
