package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.auth.LoginRequestDTO;
import com.artur_sres.multinova.dto.auth.LoginResponseDTO;
import com.artur_sres.multinova.dto.auth.VerifyRequestDTO;
import com.artur_sres.multinova.exception.InvalidCredentialsException;
import com.artur_sres.multinova.exception.InvalidVerificationCodeException;
import com.artur_sres.multinova.exception.UserNotVerifiedException;
import com.artur_sres.multinova.repository.UserRepository;
import com.artur_sres.multinova.repository.UserVerificationCodeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserVerificationCodeRepository userVerificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, UserVerificationCodeRepository userVerificationCodeRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userVerificationCodeRepository = userVerificationCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new UserNotVerifiedException("Please verify your email address before logging in.");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponseDTO(token);
    }

    @Transactional
    public LoginResponseDTO verify(VerifyRequestDTO dto) {
        User user = userRepository.findByEmail(dto.email)
                .orElseThrow(() -> new InvalidVerificationCodeException("Invalid or expired verification code."));

        var verificationCode = userVerificationCodeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidVerificationCodeException("Invalid or expired verification code."));

        if (!verificationCode.getExpiresAt().isAfter(LocalDateTime.now())
                || !passwordEncoder.matches(dto.code, verificationCode.getCode())) {
            throw new InvalidVerificationCodeException("Invalid or expired verification code.");
        }

        user.setIsVerified(true);
        userVerificationCodeRepository.delete(verificationCode);

        return new LoginResponseDTO(jwtService.generateToken(user));
    }
}
