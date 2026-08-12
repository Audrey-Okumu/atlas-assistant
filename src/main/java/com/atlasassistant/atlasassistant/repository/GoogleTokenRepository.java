package com.atlasassistant.atlasassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atlasassistant.atlasassistant.model.GoogleToken;
import com.atlasassistant.atlasassistant.model.User;

public interface GoogleTokenRepository extends JpaRepository<GoogleToken, Long> {
    GoogleToken findByUser(User user);
}