package com.bank.banking_api.service;

import com.bank.banking_api.dto.CreateUserDTO;
import com.bank.banking_api.dto.UserResponseDTO;
import com.bank.banking_api.exception.ResourceNotFoundException;
import com.bank.banking_api.model.User;
import com.bank.banking_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDTO createUser(CreateUserDTO request) {
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .build();

        User savedUser = userRepository.save(user);

        return UserResponseDTO.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .build();
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with ID " + id + " not found"
                ));
    }

    public UserResponseDTO getUserById(Long id) {
        User user = findUserById(id);
        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }
}
