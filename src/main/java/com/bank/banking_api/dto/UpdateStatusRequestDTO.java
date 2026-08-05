package com.bank.banking_api.dto;

import com.bank.banking_api.model.Account.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequestDTO {

    @NotNull(message = "Account status is required")
    private AccountStatus status;
}
