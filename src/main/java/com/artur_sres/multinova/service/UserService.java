package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Product;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.UserCreateDTO;
import com.artur_sres.multinova.dto.UserPatchDTO;
import com.artur_sres.multinova.dto.UserResponseDTO;
import com.artur_sres.multinova.exception.EmailAlreadyRegisteredException;
import com.artur_sres.multinova.exception.UserNotFoundException;
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

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserResponseDTO create(UserCreateDTO dto){
        if (existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyRegisteredException("A user with this email address already exists.");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User saved = userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setEmail(saved.getEmail());
        response.setCreatedAt(saved.getCreatedAt());

        return response;
    }

    public UserResponseDTO searchByID(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    public UserResponseDTO update(Long id, UserPatchDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found."));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
                throw new EmailAlreadyRegisteredException("A user with this email address already exists.");
            }
            user.setEmail(dto.getEmail());
        }

        userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }
}
