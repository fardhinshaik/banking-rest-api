package com.bank.banking_api.service;

import com.bank.banking_api.dto.AccountResponseDTO;
import com.bank.banking_api.dto.CreateAccountDTO;
import com.bank.banking_api.dto.DepositRequestDTO;
import com.bank.banking_api.dto.WithdrawRequestDTO;
import com.bank.banking_api.exception.InsufficientBalanceException;
import com.bank.banking_api.exception.ResourceNotFoundException;
import com.bank.banking_api.model.Account;
import com.bank.banking_api.model.Account.AccountStatus;
import com.bank.banking_api.model.Transaction;
import com.bank.banking_api.model.TransactionStatus;
import com.bank.banking_api.model.TransactionType;
import com.bank.banking_api.model.User;
import com.bank.banking_api.repository.AccountRepository;
import com.bank.banking_api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Transactional
    public AccountResponseDTO createAccount(CreateAccountDTO request) {
        User user = userService.findUserById(request.getUserId());

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .balance(request.getInitialDeposit())
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(account);

        return mapToDTO(savedAccount);
    }

    public AccountResponseDTO getAccountByNumber(String accountNumber) {
        Account account = findAccountByNumber(accountNumber);

        return mapToDTO(account);
    }

    @Transactional
    public AccountResponseDTO deposit(DepositRequestDTO request) {
        Account account = findAccountByNumberWithLock(request.getAccountNumber());
        ensureAccountIsActive(account);

        account.setBalance(account.getBalance().add(request.getAmount()));

        Account savedAccount = accountRepository.save(account);
        recordDeposit(savedAccount, request.getAmount());

        return mapToDTO(savedAccount);
    }

    @Transactional
    public AccountResponseDTO withdraw(WithdrawRequestDTO request) {
        Account account = findAccountByNumberWithLock(request.getAccountNumber());
        ensureAccountIsActive(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient funds for withdrawal");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));

        Account savedAccount = accountRepository.save(account);
        recordWithdrawal(savedAccount, request.getAmount());

        return mapToDTO(savedAccount);
    }

    @Transactional
    public AccountResponseDTO updateAccountStatus(String accountNumber, AccountStatus status) {
        Account account = findAccountByNumber(accountNumber);
        account.setStatus(status);

        return mapToDTO(accountRepository.save(account));
    }

    public Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account with number " + accountNumber + " not found"
                ));
    }

    public Account findAccountByNumberWithLock(String accountNumber) {
        return accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account with number " + accountNumber + " not found"
                ));
    }

    public void ensureAccountIsActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not ACTIVE");
        }
    }

    public AccountResponseDTO mapToDTO(Account account) {
        return AccountResponseDTO.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .ownerName(account.getUser().getFullName())
                .ownerEmail(account.getUser().getEmail())
                .status(account.getStatus())
                .build();
    }

    private void recordDeposit(Account account, java.math.BigDecimal amount) {
        Transaction transaction = Transaction.builder()
                .toAccountNumber(account.getAccountNumber())
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.SUCCESS)
                .build();

        transactionRepository.save(transaction);
    }

    private void recordWithdrawal(Account account, java.math.BigDecimal amount) {
        Transaction transaction = Transaction.builder()
                .fromAccountNumber(account.getAccountNumber())
                .amount(amount)
                .type(TransactionType.WITHDRAWAL)
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.SUCCESS)
                .build();

        transactionRepository.save(transaction);
    }

    private String generateAccountNumber() {
        return "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
