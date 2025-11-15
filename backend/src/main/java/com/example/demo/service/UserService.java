package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.response.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<UserResponse> getAllUserExceptSelf(Authentication authentication) {

        return userRepository.findAllUsersExceptSelf(UUID.fromString(authentication.getName())).stream()
                .map(userMapper::toUserResponse).toList();
    }

    public User updateUser(UUID id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFirstName(userDetails.getFirstName());
                    user.setLastName(userDetails.getLastName());
                    user.setEmail(userDetails.getEmail());
                    user.setRoles(userDetails.getRoles());
                    user.setIsActive(userDetails.getIsActive());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
        logger.info("Deleted user with id: {}", id);
    }

    public User createOrUpdateUserFromKeycloak(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();

        String keycloakId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        Optional<User> existingUser = userRepository.findByKeycloakId(keycloakId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user information if it has changed
            boolean updated = false;

            // if (!username.equals(user.getUsername())) {
            // user.setUsername(username);
            // updated = true;
            // }
            if (!email.equals(user.getEmail())) {
                user.setEmail(email);
                updated = true;
            }
            if (!firstName.equals(user.getFirstName())) {
                user.setFirstName(firstName);
                updated = true;
            }
            if (!lastName.equals(user.getLastName())) {
                user.setLastName(lastName);
                updated = true;
            }

            if (updated) {
                user = userRepository.save(user);
                logger.info("Updated user from Keycloak: {}", username);
            }

            return user;
        } else {
            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setKeycloakId(keycloakId);
            newUser.setRoles(Set.of(User.Role.USER)); // Default role
            newUser = userRepository.save(newUser);
            logger.info("Created new user from Keycloak: {}", username);
            return newUser;
        }
    }

    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByFirstNameContainingOrLastNameContaining(searchTerm);
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> getActiveUsers() {
        return userRepository.findByIsActive(true);
    }

    public Long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}