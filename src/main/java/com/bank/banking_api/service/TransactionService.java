package com.bank.banking_api.service;

import com.bank.banking_api.dto.TransactionResponseDTO;
import com.bank.banking_api.dto.TransferRequestDTO;
import com.bank.banking_api.exception.InsufficientBalanceException;
import com.bank.banking_api.model.Account;
import com.bank.banking_api.model.Transaction;
import com.bank.banking_api.model.TransactionStatus;
import com.bank.banking_api.model.TransactionType;
import com.bank.banking_api.repository.AccountRepository;
import com.bank.banking_api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @Transactional(noRollbackFor = {IllegalArgumentException.class, InsufficientBalanceException.class})
    public TransactionResponseDTO transferFunds(TransferRequestDTO request, String authenticatedUsername) {
        String fromAccountNumber = request.getFromAccountNumber();
        String toAccountNumber = request.getToAccountNumber();

        try {
            if (fromAccountNumber.equals(toAccountNumber)) {
                throw new IllegalArgumentException("Cannot transfer funds to the same account");
            }

            Account fromAccount = accountService.findAccountByNumberWithLock(fromAccountNumber);

            // SECURITY FIX: Ownership Check
            if (!fromAccount.getUser().getUsername().equals(authenticatedUsername)) {
                throw new IllegalArgumentException("Unauthorized: You do not own source account " + fromAccountNumber);
            }

            Account toAccount = accountService.findAccountByNumberWithLock(toAccountNumber);

            accountService.ensureAccountIsActive(fromAccount);
            accountService.ensureAccountIsActive(toAccount);

            if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient balance in account " + fromAccountNumber
                );
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            Transaction transaction = Transaction.builder()
                    .fromAccountNumber(fromAccountNumber)
                    .toAccountNumber(toAccountNumber)
                    .amount(request.getAmount())
                    .type(TransactionType.TRANSFER)
                    .timestamp(LocalDateTime.now())
                    .status(TransactionStatus.SUCCESS)
                    .build();

            Transaction savedTransaction = transactionRepository.save(transaction);
            return mapToDTO(savedTransaction);

        } catch (Exception ex) {
            Transaction failedTransaction = Transaction.builder()
                    .fromAccountNumber(fromAccountNumber)
                    .toAccountNumber(toAccountNumber)
                    .amount(request.getAmount())
                    .type(TransactionType.TRANSFER)
                    .timestamp(LocalDateTime.now())
                    .status(TransactionStatus.FAILED)
                    .failureReason(ex.getMessage())
                    .build();

            transactionRepository.save(failedTransaction);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionHistory(String accountNumber, String authenticatedUsername) {
        Account account = accountService.findAccountByNumber(accountNumber);

        // Ownership Check (Triggers lazy loading of User)
        if (!account.getUser().getUsername().equals(authenticatedUsername)) {
            throw new IllegalArgumentException("Unauthorized: You cannot view transaction history for this account");
        }

        List<Transaction> transactions = transactionRepository
                .findByFromAccountNumberOrToAccountNumberOrderByTimestampDesc(
                        accountNumber,
                        accountNumber
                );

        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TransactionResponseDTO mapToDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getId())
                .fromAccountNumber(transaction.getFromAccountNumber())
                .toAccountNumber(transaction.getToAccountNumber())
                .amount(transaction.getAmount())
                .type(transaction.getType().toString())
                .status(transaction.getStatus().toString())
                .timestamp(transaction.getTimestamp())
                .failureReason(transaction.getFailureReason())
                .build();
    }
}