package com.collaborativeediting.usermanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testRegisterUser() {
        User user = userService.registerUser("testuser", "password", "test@example.com", "Test", "User");
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }

    @Test
    public void testAuthenticateUser() {
        userService.registerUser("testuser2", "password", "test2@example.com", "Test", "User");
        String token = userService.authenticateUser("testuser2", "password");
        assertNotNull(token);
    }
}
