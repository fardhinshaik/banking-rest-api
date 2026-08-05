package com.bank.banking_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDTO {

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be blank")
    private String email;
}
