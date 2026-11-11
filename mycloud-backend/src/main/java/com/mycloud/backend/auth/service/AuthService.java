package com.mycloud.backend.auth.service;

import com.mycloud.backend.auth.exception.EmailAlreadyExistsException;
import com.mycloud.backend.auth.dto.LoginResponse;
import com.mycloud.backend.auth.exception.InvalidCredentialsException;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(String email, String firstName, String lastName, String password) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException();
        }
        String passwordHash = passwordEncoder.encode(password);
        UUID id = UUID.randomUUID();
        User user = new User(id, email, firstName, lastName, passwordHash);
        userRepository.save(user);
    }

    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtService.createToken(user.getId());
        return new LoginResponse("Đăng nhập thành công", token, "Bearer", JwtService.LIFETIME_SECONDS);
    }
}
