package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Product;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.exception.EmailAlreadyRegisteredException;
import com.artur_sres.multinova.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User create(User u){
        if (existsByEmail(u.getEmail())) {
            throw new EmailAlreadyRegisteredException("Já existe um usuário cadastrado com o email: " + u.getEmail());
        }
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        return userRepository.save(u);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
