package com.example.spring_security_demo_poc.service;

import com.example.spring_security_demo_poc.entity.User;
import com.example.spring_security_demo_poc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
