package com.atlasassistant.atlasassistant.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.atlasassistant.atlasassistant.model.User;
import com.atlasassistant.atlasassistant.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HomeController homeController;

    @Test
    void getAllUsers_returnsUsersFromRepository() {
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setEmail("fake@example.com");
        fakeUser.setName("Fake User");

        when(userRepository.findAll()).thenReturn(List.of(fakeUser));

        List<User> result = homeController.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("fake@example.com", result.get(0).getEmail());
    }

    @Test
    void health_returnsHealthyMessage() {
        String result = homeController.health();

        assertEquals("Atlas Assistant is healthy", result);
    }
}