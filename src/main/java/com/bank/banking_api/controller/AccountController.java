package com.bank.banking_api.controller;

import com.bank.banking_api.dto.AccountResponseDTO;
import com.bank.banking_api.dto.CreateAccountDTO;
import com.bank.banking_api.dto.DepositRequestDTO;
import com.bank.banking_api.dto.WithdrawRequestDTO;
import com.bank.banking_api.model.Account.AccountStatus;
import com.bank.banking_api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // ADMIN ONLY
    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    // Creates account bound to the currently authenticated user
    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(
            Authentication authentication,
            @RequestBody CreateAccountDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccountForUser(authentication.getName(), request));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> getAccount(
            @PathVariable String accountNumber,
            Authentication authentication) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber, authentication.getName()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponseDTO> deposit(
            Authentication authentication,
            @RequestBody DepositRequestDTO request) {
        return ResponseEntity.ok(accountService.deposit(request, authentication.getName()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponseDTO> withdraw(
            Authentication authentication,
            @RequestBody WithdrawRequestDTO request) {
        return ResponseEntity.ok(accountService.withdraw(request, authentication.getName()));
    }

    // ADMIN ONLY
    @PatchMapping("/{accountNumber}/status")
    public ResponseEntity<AccountResponseDTO> updateStatus(
            @PathVariable String accountNumber,
            @RequestParam AccountStatus status) {
        return ResponseEntity.ok(accountService.updateAccountStatus(accountNumber, status));
    }
}