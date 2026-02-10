package com.pocas.repo;

import com.pocas.entity.Account;
import com.pocas.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomer(Customer customer);
    // Fetch all accounts for a given customer
    List<Account> findByCustomerId(Long customerId);

    }