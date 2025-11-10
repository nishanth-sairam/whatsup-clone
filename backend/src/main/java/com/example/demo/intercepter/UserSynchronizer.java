package com.example.demo.intercepter;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSynchronizer {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public User synchronizedWithIdp(Jwt token) {
        log.info("Synchronizing user with IDP: {}", token.getSubject());

        Optional<String> emailOpt = getUserEmail(token);
        if (emailOpt.isPresent()) {
            String email = emailOpt.get();
            log.info("Synchronizing user having email {}", email);
            Optional<User> optUser = userRepository.findByEmail(email);
            User user = userMapper.fromTokenAttributes(token.getClaims());
            optUser.ifPresent(value -> {
                user.setId(value.getId());
            });
            log.info("User synchronized with IDP: {}", user);
            return userRepository.save(user);
        }
        return null;
    }

    private Optional<String> getUserEmail(Jwt token) {
        Map<String, Object> claims = token.getClaims();

        if (claims.containsKey("email")) {
            return Optional.of(claims.get("email").toString());
        }
        return Optional.empty();
    }
}
