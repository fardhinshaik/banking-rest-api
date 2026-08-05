package com.bank.banking_api.controller;

import com.bank.banking_api.dto.AccountResponseDTO;
import com.bank.banking_api.dto.CreateAccountDTO;
import com.bank.banking_api.dto.DepositRequestDTO;
import com.bank.banking_api.dto.UpdateStatusRequestDTO;
import com.bank.banking_api.dto.WithdrawRequestDTO;
import com.bank.banking_api.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // Added: Handles GET requests on /api/v1/accounts
    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        List<AccountResponseDTO> response = accountService.getAllAccounts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody CreateAccountDTO request) {
        AccountResponseDTO response = accountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponseDTO response = accountService.getAccountByNumber(accountNumber);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponseDTO> deposit(@Valid @RequestBody DepositRequestDTO request) {
        AccountResponseDTO response = accountService.deposit(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponseDTO> withdraw(@Valid @RequestBody WithdrawRequestDTO request) {
        AccountResponseDTO response = accountService.withdraw(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{accountNumber}/status")
    public ResponseEntity<AccountResponseDTO> updateAccountStatus(
            @PathVariable String accountNumber,
            @Valid @RequestBody UpdateStatusRequestDTO request) {

        AccountResponseDTO response = accountService.updateAccountStatus(
                accountNumber,
                request.getStatus()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}