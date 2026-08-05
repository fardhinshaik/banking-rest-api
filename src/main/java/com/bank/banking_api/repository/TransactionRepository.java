package com.bank.banking_api.repository;

import com.bank.banking_api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountNumberOrToAccountNumberOrderByTimestampDesc(
            String fromAccountNumber, 
            String toAccountNumber
    );
}
