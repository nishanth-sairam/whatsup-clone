package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();

            User user = userService.createOrUpdateUserFromKeycloak(authentication);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("keycloakId", jwt.getSubject());
            userInfo.put("username", jwt.getClaimAsString("preferred_username"));
            userInfo.put("email", jwt.getClaimAsString("email"));
            userInfo.put("firstName", jwt.getClaimAsString("given_name"));
            userInfo.put("lastName", jwt.getClaimAsString("family_name"));
            userInfo.put("roles", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            userInfo.put("isActive", user.getIsActive());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("updatedAt", user.getLastModifiedAt());

            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            logger.error("Error retrieving user info", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve user information"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(Authentication authentication) {
        try {
            User user = userService.createOrUpdateUserFromKeycloak(authentication);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error retrieving user profile", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(@RequestBody User userDetails, Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String keycloakId = jwt.getSubject();

            User existingUser = userService.getUserByKeycloakId(keycloakId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            existingUser.setFirstName(userDetails.getFirstName());
            existingUser.setLastName(userDetails.getLastName());

            User updatedUser = userService.updateUser(existingUser.getId(), existingUser);

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> getTokenInfo(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("subject", jwt.getSubject());
            tokenInfo.put("issuer", jwt.getIssuer().toString());
            tokenInfo.put("audience", jwt.getAudience());
            tokenInfo.put("expiresAt", jwt.getExpiresAt());
            tokenInfo.put("issuedAt", jwt.getIssuedAt());
            tokenInfo.put("tokenValue", jwt.getTokenValue().substring(0, 20) + "...");

            return ResponseEntity.ok(tokenInfo);
        } catch (Exception e) {
            logger.error("Error retrieving token info", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve token information"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {

        logger.info("Logout request received");
        return ResponseEntity.ok(Map.of("message", "Logout successful. Please clear your tokens."));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();

            Map<String, Object> validation = new HashMap<>();
            validation.put("valid", true);
            validation.put("username", jwt.getClaimAsString("preferred_username"));
            validation.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            validation.put("expiresAt", jwt.getExpiresAt());

            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "error", "Invalid token"));
        }
    }
}