package com.restaurant.api.mapper;

import org.modelmapper.ModelMapper;

public abstract class BaseMapper<E, D> {

    protected final ModelMapper modelMapper;
    private final Class<E> entityClass;

    protected BaseMapper(ModelMapper modelMapper, Class<E> entityClass) {
        this.modelMapper = modelMapper;
        this.entityClass = entityClass;
    }

    public <T> T toDto(E entity, Class<T> dtoClass) {
        return modelMapper.map(entity, dtoClass);
    }

    public E toEntity(D dto) {
        return modelMapper.map(dto, entityClass);
    }
}
