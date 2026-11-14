package com.mycloud.backend.users.service;

import com.mycloud.backend.users.dto.UserResponse;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.exception.InvalidSessionException;
import com.mycloud.backend.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        return UserResponse.from(requireUser(userId));
    }

    public User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(InvalidSessionException::new);
    }
}
