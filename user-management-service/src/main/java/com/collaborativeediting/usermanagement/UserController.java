package com.collaborativeediting.usermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request.getUsername(), request.getPassword(), request.getEmail(), request.getFirstName(), request.getLastName());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = userService.authenticateUser(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestHeader("Authorization") String token) {
        String username = extractUsernameFromToken(token);
        User user = userService.getUserProfile(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestHeader("Authorization") String token, @RequestBody UpdateProfileRequest request) {
        String username = extractUsernameFromToken(token);
        User user = userService.updateUserProfile(username, request.getFirstName(), request.getLastName(), request.getEmail());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String token) {
        String username = extractUsernameFromToken(token);
        if (!userService.hasPermission(username, "MANAGE_USERS")) {
            return ResponseEntity.status(403).build();
        }
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long userId, @RequestBody UpdateRoleRequest request, @RequestHeader("Authorization") String token) {
        String requestingUsername = extractUsernameFromToken(token);
        if (!userService.hasPermission(requestingUsername, "MANAGE_USERS")) {
            return ResponseEntity.status(403).build();
        }

        // Find user by ID
        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        User updatedUser = userService.updateUserRole(userToUpdate.getUsername(), request.getRole(), requestingUsername);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/permissions")
    public ResponseEntity<UserPermissions> getUserPermissions(@RequestHeader("Authorization") String token) {
        String username = extractUsernameFromToken(token);
        User user = userService.getUserProfile(username);

        UserPermissions permissions = new UserPermissions();
        permissions.setRole(user.getRole());
        permissions.setCanCreateDocument(userService.hasPermission(username, "CREATE_DOCUMENT"));
        permissions.setCanEditDocument(userService.hasPermission(username, "EDIT_DOCUMENT"));
        permissions.setCanDeleteDocument(userService.hasPermission(username, "DELETE_DOCUMENT"));
        permissions.setCanManageUsers(userService.hasPermission(username, "MANAGE_USERS"));
        permissions.setCanViewAllDocuments(userService.hasPermission(username, "VIEW_ALL_DOCUMENTS"));

        return ResponseEntity.ok(permissions);
    }

    private String extractUsernameFromToken(String token) {
        // Simple extraction, in real app use JwtUtil
        return token.replace("Bearer ", "");
    }

    // DTOs
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String firstName;
        private String lastName;

        // getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private String token;

        public LoginResponse(String token) { this.token = token; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String email;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class UpdateRoleRequest {
        private UserRole role;

        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
    }

    public static class UserPermissions {
        private UserRole role;
        private boolean canCreateDocument;
        private boolean canEditDocument;
        private boolean canDeleteDocument;
        private boolean canManageUsers;
        private boolean canViewAllDocuments;

        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }

        public boolean isCanCreateDocument() { return canCreateDocument; }
        public void setCanCreateDocument(boolean canCreateDocument) { this.canCreateDocument = canCreateDocument; }

        public boolean isCanEditDocument() { return canEditDocument; }
        public void setCanEditDocument(boolean canEditDocument) { this.canEditDocument = canEditDocument; }

        public boolean isCanDeleteDocument() { return canDeleteDocument; }
        public void setCanDeleteDocument(boolean canDeleteDocument) { this.canDeleteDocument = canDeleteDocument; }

        public boolean isCanManageUsers() { return canManageUsers; }
        public void setCanManageUsers(boolean canManageUsers) { this.canManageUsers = canManageUsers; }

        public boolean isCanViewAllDocuments() { return canViewAllDocuments; }
        public void setCanViewAllDocuments(boolean canViewAllDocuments) { this.canViewAllDocuments = canViewAllDocuments; }
    }
}
