package com.restaurant.api.mapper;

import com.restaurant.api.dto.FileDto;
import com.restaurant.api.entity.FileEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class FileMapper extends BaseMapper<FileEntity, FileDto> {
    public FileMapper(ModelMapper modelMapper) {
        super(modelMapper, FileEntity.class);
    }

    public FileDto.Response toResponse(FileEntity entity) {
        return super.toDto(entity, FileDto.Response.class);
    }
}
