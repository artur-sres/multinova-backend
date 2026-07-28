package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.auth.ForgotPasswordRequestDTO;
import com.artur_sres.multinova.dto.auth.LoginRequestDTO;
import com.artur_sres.multinova.dto.auth.LoginResponseDTO;
import com.artur_sres.multinova.dto.auth.ResetPasswordRequestDTO;
import com.artur_sres.multinova.dto.auth.VerifyRequestDTO;
import com.artur_sres.multinova.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(dto));
    }

    @PostMapping("/verify")
    public ResponseEntity<LoginResponseDTO> verifyUserEmail(@Valid @RequestBody VerifyRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.verify(dto));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        authService.requestPasswordReset(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO dto) {
        authService.resetPassword(dto);
        return ResponseEntity.ok().build();
    }

}
