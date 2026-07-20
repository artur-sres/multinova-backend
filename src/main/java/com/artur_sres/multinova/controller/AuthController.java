package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.auth.LoginRequestDTO;
import com.artur_sres.multinova.dto.auth.LoginResponseDTO;
import com.artur_sres.multinova.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://127.0.0.1:5500/")
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

}
