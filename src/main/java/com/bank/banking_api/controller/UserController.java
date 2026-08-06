package com.bank.banking_api.controller;

import com.bank.banking_api.dto.CreateUserDTO;
import com.bank.banking_api.dto.UserResponseDTO;
import com.bank.banking_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Public Guest Sign-Up
    @PostMapping
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody CreateUserDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}