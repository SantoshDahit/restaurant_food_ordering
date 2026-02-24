package com.restaurant.api.repository.user;

import com.restaurant.api.dto.UserDto;
import com.restaurant.api.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    private final UserQueryRepository userQueryRepository;

    @Override
    public Optional<User> findByCode(String code) {
        return userJpaRepository.findByCodeAndDeleteAtIsNull(code);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmailAndDeleteAtIsNull(email);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Page<User> search(UserDto.SearchRequest searchRequest, Pageable pageable) {
        return userQueryRepository.search(searchRequest, pageable);
    }
}
