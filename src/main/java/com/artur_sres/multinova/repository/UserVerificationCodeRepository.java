package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.UserVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVerificationCodeRepository extends JpaRepository<UserVerificationCode, Long> {
    Optional<UserVerificationCode> findByUserId(Long userId);
    Optional<UserVerificationCode> findByUserIdAndPurpose(Long userId, String purpose);
}
