package com.restaurant.api.mapper;

import com.restaurant.api.dto.FileDto;
import com.restaurant.api.entity.File;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class FileMapper extends BaseMapper<File, FileDto> {
    public FileMapper(ModelMapper modelMapper) {
        super(modelMapper, File.class);
    }

    public FileDto.Response toResponse(File entity) {
        return super.toDto(entity, FileDto.Response.class);
    }
}
