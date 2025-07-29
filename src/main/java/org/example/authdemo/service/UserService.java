package org.example.authdemo.service;

import jakarta.transaction.Transactional;
import org.example.authdemo.dto.RegisterRequest;
import org.example.authdemo.exception.CustomException;
import org.example.authdemo.model.Role;
import org.example.authdemo.model.User;
import org.example.authdemo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest req) {
        if(userRepository.existsByLogin(req.getLogin())) {
            throw new CustomException("Login is already taken", HttpStatus.BAD_REQUEST);
        }
        if(userRepository.existsByEmail(req.getEmail())) {
            throw new CustomException("Email is already registered", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setLogin(req.getLogin());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRoles(Set.of(Role.GUEST));

        return userRepository.save(user);
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
