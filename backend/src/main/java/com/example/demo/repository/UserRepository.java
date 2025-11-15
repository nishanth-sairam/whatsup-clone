package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query(value = "SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    @Query(value = "SELECT u FROM User u WHERE u.id = :publicId")
    Optional<User> findByPublicId(UUID publicId);

    @Query(value = "SELECT u FROM User u WHERE u.id <> :publicId")
    List<User> findAllUsersExceptSelf(UUID publicId);

    Optional<User> findByKeycloakId(String keycloakId);

    boolean existsByEmail(String email);

    List<User> findByIsActive(Boolean isActive);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name%")
    List<User> findByFirstNameContainingOrLastNameContaining(@Param("name") String name);

    @Query("SELECT u FROM User u WHERE :role MEMBER OF u.roles")
    List<User> findByRole(@Param("role") User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();
}