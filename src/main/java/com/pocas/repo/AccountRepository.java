package com.pocas.repo;

import com.pocas.entity.CollectionAccount;
import com.pocas.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<CollectionAccount, Long> {

    Optional<CollectionAccount> findByAccountNumber(String accountNumber);

    List<CollectionAccount> findByCustomer(Customer customer);
    // Fetch all accounts for a given customer
    List<CollectionAccount> findByCustomerId(Long customerId);

    }