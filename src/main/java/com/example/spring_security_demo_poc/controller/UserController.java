package com.example.spring_security_demo_poc.controller;

import com.example.spring_security_demo_poc.entity.Role;
import com.example.spring_security_demo_poc.repository.RoleRepository;
import com.example.spring_security_demo_poc.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_security_demo_poc.entity.User;
import com.example.spring_security_demo_poc.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class UserController {
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserService userService;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        // Check if user already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        Set<Role> persistedRoles = new HashSet<>();
        for (Role role : user.getRoles()) {
            Role persistedRole = roleRepository.findByName(role.getName());
            if (persistedRole == null) {
                persistedRole = roleRepository.save(role); // Save if not already present
            }
            persistedRoles.add(persistedRole);
        }
        user.setRoles(persistedRoles);

        // Save new user with encoded password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users") // Fetch all users
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
