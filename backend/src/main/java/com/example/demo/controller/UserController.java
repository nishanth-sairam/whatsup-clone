package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.response.UserResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsersExceptSelf(Authentication authentication) {
        List<UserResponse> users = userService.getAllUserExceptSelf(authentication);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            logger.info("Retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error retrieving users", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving user with id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        try {
            Optional<User> user = userService.getUserByEmail(email);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving user with email: {}", email, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        try {
            // // Check if user already exists
            // if (userService.existsByUsername(user.getUsername())) {
            // return ResponseEntity.badRequest().build();
            // }
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest().build();
            }

            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @Valid @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            logger.error("Error updating user with id: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating user with id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID id) {
        try {
            userService.deleteUser(id);
            logger.info("Deleted user with id: {}", id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting user with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to delete user"));
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String q) {
        try {
            List<User> users = userService.searchUsers(q);
            logger.info("Found {} users matching search term: {}", users.size(), q);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error searching users with term: {}", q, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable User.Role role) {
        try {
            List<User> users = userService.getUsersByRole(role);
            logger.info("Found {} users with role: {}", users.size(), role);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error retrieving users with role: {}", role, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<List<User>> getActiveUsers() {
        try {
            List<User> users = userService.getActiveUsers();
            logger.info("Retrieved {} active users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error retrieving active users", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userService.getAllUsers().size());
            stats.put("activeUsers", userService.getActiveUserCount());
            stats.put("adminUsers", userService.getUsersByRole(User.Role.ADMIN).size());
            stats.put("moderatorUsers", userService.getUsersByRole(User.Role.MODERATOR).size());
            stats.put("regularUsers", userService.getUsersByRole(User.Role.USER).size());

            logger.info("Retrieved user statistics");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error retrieving user statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> toggleUserStatus(@PathVariable UUID id) {
        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setIsActive(!user.getIsActive());
                User updatedUser = userService.updateUser(id, user);
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error toggling user status for id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}