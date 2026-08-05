package com.bank.banking_api.dto;

import com.bank.banking_api.model.Account.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDTO {

    private String accountNumber;
    private BigDecimal balance;
    private String ownerName;
    private String ownerEmail;
    private AccountStatus status;
}
