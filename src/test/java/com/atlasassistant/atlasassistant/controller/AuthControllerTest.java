package com.atlasassistant.atlasassistant.controller;

import com.atlasassistant.atlasassistant.config.JwtUtil;
import com.atlasassistant.atlasassistant.model.User;
import com.atlasassistant.atlasassistant.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_hashesPasswordBeforeSaving() {
        User inputUser = new User();
        inputUser.setEmail("newuser@example.com");
        inputUser.setPassword("plaintext123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("newuser@example.com");
        savedUser.setPassword("$2a$10$hashedvalue");

        when(passwordEncoder.encode("plaintext123")).thenReturn("$2a$10$hashedvalue");
        when(userRepository.save(inputUser)).thenReturn(savedUser);

        User result = authController.register(inputUser);

        assertEquals("$2a$10$hashedvalue", result.getPassword());
        assertEquals("newuser@example.com", result.getEmail());
    }
}