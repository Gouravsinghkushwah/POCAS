package com.pocas.service;

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
}
