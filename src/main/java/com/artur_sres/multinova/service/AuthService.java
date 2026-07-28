package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.domain.UserVerificationCode;
import com.artur_sres.multinova.dto.auth.ForgotPasswordRequestDTO;
import com.artur_sres.multinova.dto.auth.LoginRequestDTO;
import com.artur_sres.multinova.dto.auth.LoginResponseDTO;
import com.artur_sres.multinova.dto.auth.ResetPasswordRequestDTO;
import com.artur_sres.multinova.dto.auth.VerifyRequestDTO;
import com.artur_sres.multinova.exception.InvalidCredentialsException;
import com.artur_sres.multinova.exception.InvalidVerificationCodeException;
import com.artur_sres.multinova.exception.UserNotVerifiedException;
import com.artur_sres.multinova.repository.UserRepository;
import com.artur_sres.multinova.repository.UserVerificationCodeRepository;
import com.artur_sres.multinova.util.VerificationCodeGenerator;
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
    private final EmailService emailService;
    private final VerificationCodeGenerator verificationCodeGenerator;

    public AuthService(UserRepository userRepository, UserVerificationCodeRepository userVerificationCodeRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService, VerificationCodeGenerator verificationCodeGenerator) {
        this.userRepository = userRepository;
        this.userVerificationCodeRepository = userVerificationCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationCodeGenerator = verificationCodeGenerator;
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

        var verificationCode = userVerificationCodeRepository.findByUserIdAndPurpose(user.getId(), "ACCOUNT_VERIFICATION")
                .orElseThrow(() -> new InvalidVerificationCodeException("Invalid or expired verification code."));

        if (!verificationCode.getExpiresAt().isAfter(LocalDateTime.now())
                || !passwordEncoder.matches(dto.code, verificationCode.getCode())) {
            throw new InvalidVerificationCodeException("Invalid or expired verification code.");
        }

        user.setIsVerified(true);
        userVerificationCodeRepository.delete(verificationCode);

        return new LoginResponseDTO(jwtService.generateToken(user));
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequestDTO dto) {
        var userOptional = userRepository.findByEmail(dto.getEmail());
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();
        userVerificationCodeRepository.findByUserIdAndPurpose(user.getId(), "PASSWORD_RESET")
                .ifPresent(userVerificationCodeRepository::delete);

        String plainTextCode = verificationCodeGenerator.generate();
        UserVerificationCode verificationCode = new UserVerificationCode();
        verificationCode.setUser(user);
        verificationCode.setCode(passwordEncoder.encode(plainTextCode));
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        verificationCode.setPurpose("PASSWORD_RESET");
        userVerificationCodeRepository.save(verificationCode);

        emailService.sendPasswordResetCode(user.getEmail(), plainTextCode);
    }

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public void resetPassword(ResetPasswordRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new com.artur_sres.multinova.exception.UserNotFoundException("User not found."));

        UserVerificationCode verificationCode = userVerificationCodeRepository
                .findByUserIdAndPurpose(user.getId(), "PASSWORD_RESET")
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired password reset code."));

        if (!verificationCode.getExpiresAt().isAfter(LocalDateTime.now())) {
            userVerificationCodeRepository.delete(verificationCode);
            throw new InvalidCredentialsException("Invalid or expired password reset code.");
        }

        if (!passwordEncoder.matches(dto.getCode(), verificationCode.getCode())) {
            throw new InvalidCredentialsException("Invalid or expired password reset code.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        userVerificationCodeRepository.delete(verificationCode);
    }
}
