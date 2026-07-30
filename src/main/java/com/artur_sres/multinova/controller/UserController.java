package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.user.UserRequestDTO;
import com.artur_sres.multinova.dto.user.UserPatchDTO;
import com.artur_sres.multinova.dto.user.UserResponseDTO;
import com.artur_sres.multinova.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserByID(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.searchByID(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUserByID(@PathVariable Long id, @Valid @RequestBody UserPatchDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.update(id, dto));
    }
}
