package com.atlasassistant.atlasassistant.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atlasassistant.atlasassistant.model.User;
import com.atlasassistant.atlasassistant.repository.UserRepository;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/phone-number")
    public String setPhoneNumber(Principal principal, @RequestBody Map<String, String> body) {
        String phoneNumber = body.get("phoneNumber");

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        User user = userRepository.findByEmail(principal.getName());
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);

        return "Phone number saved. You can now message Atlas Assistant on WhatsApp.";
    }
}