package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.domain.UserVerificationCode;
import com.artur_sres.multinova.dto.user.UserCreateDTO;
import com.artur_sres.multinova.dto.user.UserPatchDTO;
import com.artur_sres.multinova.dto.user.UserResponseDTO;
import com.artur_sres.multinova.exception.AccessDeniedException;
import com.artur_sres.multinova.exception.EmailAlreadyRegisteredException;
import com.artur_sres.multinova.exception.UserNotFoundException;
import com.artur_sres.multinova.mapper.UserMapper;
import com.artur_sres.multinova.repository.UserRepository;
import com.artur_sres.multinova.repository.UserVerificationCodeRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserVerificationCodeRepository userVerificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserVerificationCodeRepository userVerificationCodeRepository,
                       PasswordEncoder passwordEncoder, EmailService emailService,
                       AuthenticatedUserProvider authenticatedUserProvider, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userVerificationCodeRepository = userVerificationCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.userMapper = userMapper;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public UserResponseDTO create(UserCreateDTO dto){
        User user = userRepository.findByEmail(dto.getEmail()).orElseGet(User::new);

        if (user.getId() != null && Boolean.TRUE.equals(user.getIsVerified())) {
            throw new EmailAlreadyRegisteredException("A user with this email address already exists.");
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User saved = userRepository.save(user);
        userVerificationCodeRepository.findByUserId(saved.getId()).ifPresent(userVerificationCodeRepository::delete);

        String plainCode = generateVerificationCode();
        UserVerificationCode verificationCode = new UserVerificationCode();
        verificationCode.setUser(saved);
        verificationCode.setCode(passwordEncoder.encode(plainCode));
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        userVerificationCodeRepository.save(verificationCode);

        emailService.sendVerificationCode(saved.getEmail(), plainCode);

        return userMapper.toResponseDTO(saved);
    }

    private String generateVerificationCode() {
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(5);
        for (int index = 0; index < 5; index++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    public UserResponseDTO searchByID(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        if (!currentUser.getId().equals(id)) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserPatchDTO dto) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        if (!currentUser.getId().equals(id)) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
                throw new EmailAlreadyRegisteredException("A user with this email address already exists.");
            }
            user.setEmail(dto.getEmail());
        }

        return userMapper.toResponseDTO(user);
    }
}
