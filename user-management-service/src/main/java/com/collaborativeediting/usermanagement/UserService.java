package com.collaborativeediting.usermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User registerUser(String username, String password, String email, String firstName, String lastName) {
        if (userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        User user = new User(username, passwordEncoder.encode(password), email, firstName, lastName);
        return userRepository.save(user);
    }

    public User registerUserWithRole(String username, String password, String email, String firstName, String lastName, UserRole role) {
        if (userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        User user = new User(username, passwordEncoder.encode(password), email, firstName, lastName);
        user.setRole(role);
        return userRepository.save(user);
    }

    public String authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return jwtUtil.generateToken(username);
        }
        throw new RuntimeException("Invalid credentials");
    }

    public User getUserProfile(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUserProfile(String username, String firstName, String lastName, String email) {
        User user = getUserProfile(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }

    public User updateUserRole(String username, UserRole newRole, String requestingUser) {
        User requestingUserObj = getUserProfile(requestingUser);
        if (requestingUserObj.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can change user roles");
        }

        User user = getUserProfile(username);
        user.setRole(newRole);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean hasRole(String username, UserRole role) {
        User user = getUserProfile(username);
        return user.getRole() == role;
    }

    public boolean hasPermission(String username, String permission) {
        User user = getUserProfile(username);
        UserRole role = user.getRole();

        switch (permission) {
            case "CREATE_DOCUMENT":
                return role == UserRole.ADMIN || role == UserRole.EDITOR || role == UserRole.USER;
            case "EDIT_DOCUMENT":
                return role == UserRole.ADMIN || role == UserRole.EDITOR || role == UserRole.USER;
            case "DELETE_DOCUMENT":
                return role == UserRole.ADMIN || role == UserRole.EDITOR;
            case "MANAGE_USERS":
                return role == UserRole.ADMIN;
            case "VIEW_ALL_DOCUMENTS":
                return role == UserRole.ADMIN;
            default:
                return false;
        }
    }
}
