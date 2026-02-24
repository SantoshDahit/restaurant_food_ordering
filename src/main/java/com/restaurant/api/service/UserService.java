package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.UserDto;
import com.restaurant.api.entity.User;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getByCode(String code) {
        return userRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<User> search(UserDto.SearchRequest searchRequest, Pageable pageable) {
        return userRepository.search(searchRequest, pageable);
    }

    @Transactional
    public User create(UserDto.RegisterRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new ApiException(ErrorCode.USER_ALREADY_EXISTS);
        });
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(
                UuidUtil.generate(),
                request.restaurantCode(),
                request.fullName(),
                request.email(),
                request.phone(),
                hashedPassword,
                request.role()
        );
        return userRepository.save(user);
    }

    @Transactional
    public User update(String code, UserDto.PatchRequest request) {
        User user = getByCode(code);
        user.update(request.fullName(), request.phone(), request.fileCode());
        return userRepository.save(user);
    }

    @Transactional
    public User updateRestaurantCode(String code, String restaurantCode) {
        User user = getByCode(code);
        user.updateRestaurantCode(restaurantCode);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(String code) {
        User user = getByCode(code);
        user.deactivate();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
        return user;
    }
}
