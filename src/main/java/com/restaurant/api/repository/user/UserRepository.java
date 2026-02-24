package com.restaurant.api.repository.user;

import com.restaurant.api.dto.UserDto;
import com.restaurant.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByCode(String code);
    Optional<User> findByEmail(String email);
    User save(User user);
    Page<User> search(UserDto.SearchRequest searchRequest, Pageable pageable);
}
