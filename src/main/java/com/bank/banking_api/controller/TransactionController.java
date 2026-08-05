package com.bank.banking_api.controller;

import com.bank.banking_api.dto.TransactionResponseDTO;
import com.bank.banking_api.dto.TransferRequestDTO;
import com.bank.banking_api.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> transferFunds(@Valid @RequestBody TransferRequestDTO request) {
        TransactionResponseDTO response = transactionService.transferFunds(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionHistory(@PathVariable String accountNumber) {
        List<TransactionResponseDTO> response = transactionService.getTransactionHistory(accountNumber);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
