package com.pocas.service;

import com.pocas.entity.CustomerStatus;
import com.pocas.request.CustomerRequest;
import com.pocas.response.CustomerResponse;

import java.util.List;

public interface CustomerService {
    
    /**
     * Create a new Customer
     */
    CustomerResponse createCustomer(CustomerRequest request);
    
    /**
     * Get all customers
     */
    List<CustomerResponse> getAllCustomers();
    
    /**
     * Get customer by ID
     */
    CustomerResponse getCustomerById(Long id);

    /**
     * Update an customer
     */
    CustomerResponse updateCustomer(Long id, CustomerRequest request);
    
    /**
     * Update customer status (CLOSED or ACTIVE)
     */
    CustomerResponse updateCustomerStatus(Long id, CustomerStatus status);
    
    /**
     * Get all customers including CLOSED ones (admin view)
     */
    List<CustomerResponse> getAllCustomersIncludingClosed();

    /**
     * Get all closed customers
     */
    public List<CustomerResponse> getAllClosedCustomer();
}
