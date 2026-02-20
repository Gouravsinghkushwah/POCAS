package com.pocas.repo;

import com.pocas.entity.Customer;
import com.pocas.entity.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByMobileNumber(String mobileNumber);
    Optional<Customer> findByEmail(String email);
    long countByStatusNot(CustomerStatus status);
}
