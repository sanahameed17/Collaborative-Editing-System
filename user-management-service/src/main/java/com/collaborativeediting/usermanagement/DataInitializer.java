package com.collaborativeediting.usermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if it doesn't exist
        if (!userRepository.findByUsername("admin").isPresent()) {
            userService.registerUserWithRole("admin", "admin123", "admin@example.com", "Admin", "User", UserRole.ADMIN);
            System.out.println("Admin user created with username: admin, password: admin123");
        }

        // Create a regular user for testing
        if (!userRepository.findByUsername("user").isPresent()) {
            userService.registerUser("user", "user123", "user@example.com", "Regular", "User");
            System.out.println("Regular user created with username: user, password: user123");
        }
    }
}