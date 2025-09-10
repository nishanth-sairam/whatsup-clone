package com.example.demo.mapper;

import com.example.demo.model.User;
import com.example.demo.response.UserResponse;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Service
public class UserMapper {

    public User fromTokenAttributes(Map<String, Object> claims) {
        User user = new User();

        if (claims.containsKey("sub")) {
            user.setId(UUID.fromString(claims.get("sub").toString()));
            user.setKeycloakId(claims.get("sub").toString());
        }

        if (claims.containsKey("given_name")) {
            user.setFirstName(claims.get("given_name").toString());
        } else if (claims.containsKey("nickname")) {
            user.setFirstName(claims.get("nickname").toString());
        }

        if (claims.containsKey("family_name")) {
            user.setLastName(claims.get("family_name").toString());
        }

        if (claims.containsKey("email")) {
            user.setEmail(claims.get("email").toString());
        }

        // Set roles if present in claims, otherwise default to "USER"
        if (claims.containsKey("roles")) {
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof String) {
            user.setRoles(Set.of(User.Role.valueOf(rolesObj.toString())));
            } else if (rolesObj instanceof java.util.Collection<?>) {
            @SuppressWarnings("unchecked")
            java.util.Collection<Object> rolesCollection = (java.util.Collection<Object>) rolesObj;
            Set<User.Role> roleSet = new java.util.HashSet<>();
            for (Object roleObj : rolesCollection) {
                roleSet.add(User.Role.valueOf(roleObj.toString()));
            }
            user.setRoles(roleSet);
            }
        } else {
            user.setRoles(Set.of(User.Role.USER));
        }

        user.setLastSeen(LocalDateTime.now());

        return user;

    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .lastSeen(user.getLastSeen())
                .isOnline(user.isUserOnline())
                .roles(user.getRoles())
                .build();
    }
}
