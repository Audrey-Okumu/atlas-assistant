package com.atlasassistant.atlasassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atlasassistant.atlasassistant.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
     User findByEmail(String email);
     User findByPhoneNumber(String phoneNumber);
}