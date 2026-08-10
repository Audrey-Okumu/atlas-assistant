package com.atlasassistant.atlasassistant.repository;

import com.atlasassistant.atlasassistant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}