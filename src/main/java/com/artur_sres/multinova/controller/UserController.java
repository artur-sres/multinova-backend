package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://127.0.0.1:5500/")
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User u){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(u));
    }

}
