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
import com.artur_sres.multinova.util.VerificationCodeGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final VerificationCodeGenerator verificationCodeGenerator;

    public UserService(UserRepository userRepository, UserVerificationCodeRepository userVerificationCodeRepository,
                       PasswordEncoder passwordEncoder, EmailService emailService,
                       AuthenticatedUserProvider authenticatedUserProvider, UserMapper userMapper, VerificationCodeGenerator verificationCodeGenerator) {
        this.userRepository = userRepository;
        this.userVerificationCodeRepository = userVerificationCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.userMapper = userMapper;
        this.verificationCodeGenerator = verificationCodeGenerator;
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
        userVerificationCodeRepository.findByUserIdAndPurpose(saved.getId(), "ACCOUNT_VERIFICATION")
                .ifPresent(userVerificationCodeRepository::delete);

        String plainCode = verificationCodeGenerator.generate();
        UserVerificationCode verificationCode = new UserVerificationCode();
        verificationCode.setUser(saved);
        verificationCode.setCode(passwordEncoder.encode(plainCode));
        verificationCode.setPurpose("ACCOUNT_VERIFICATION");
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        userVerificationCodeRepository.save(verificationCode);

        emailService.sendVerificationCode(saved.getEmail(), plainCode);

        return userMapper.toResponseDTO(saved);
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
