package com.pocas.serviceimpl;

import com.pocas.entity.Customer;
import com.pocas.entity.CustomerStatus;
import com.pocas.repo.CustomerRepository;
import com.pocas.request.CustomerRequest;
import com.pocas.response.CustomerResponse;
import com.pocas.exception.ApiException;
import com.pocas.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    /**
     * Create a new Customer
     */
    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        try {
            // Check for duplicate mobile number or email
            checkCustomerExists(request.getMobileNumber(), request.getEmail());

            // Map DTO → Entity
            Customer customer = Customer.builder()
                    .name(request.getName())
                    .mobileNumber(request.getMobileNumber())
                    .email(request.getEmail())
                    .address(request.getAddress())
                    .accountType(request.getAccountType())
                    .status(CustomerStatus.ACTIVE)
                    .build();

            Customer saved = customerRepository.save(customer);

            // Map Entity → Response DTO
            return mapToResponse(saved);

        } catch (ApiException e) {
            logger.error("Business error while creating customer: {}", e.getMessage());
            throw e; // propagate for global handler
        } catch (Exception e) {
            logger.error("System error while creating customer: {}", e.getMessage());
            throw new ApiException("Failed to create customer. Please try again later.");
        }
    }

    /**
     * Get all customers
     */
    @Override
    public List<CustomerResponse> getAllCustomers() {
        try {
            return customerRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException("Failed to fetch customers. Please try again later.");
        }
    }

    /**
     * Get customer by ID
     */
    @Override
    public CustomerResponse getCustomerById(Long id) {
        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ApiException("Customer not found with ID " + id));
            return mapToResponse(customer);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to fetch customer. Please try again later.");
        }
    }
    
    
    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ApiException("Customer not found with ID " + id));
            
            // Update customer fields
            customer.setName(request.getName());
            customer.setMobileNumber(request.getMobileNumber());
            customer.setEmail(request.getEmail());
            customer.setAddress(request.getAddress());
            customer.setAccountType(request.getAccountType());
            
            Customer saved = customerRepository.save(customer);
            return mapToResponse(saved);

        } catch (Exception e) {
            throw new ApiException("Failed to update customer. Please try again later.");
        }
    }

    /**
     * Helper method to map Customer Entity → Response DTO
     */
    private CustomerResponse mapToResponse(Customer customer) {
        if (customer == null) return null;
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .mobileNumber(customer.getMobileNumber())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .accountType(customer.getAccountType())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    /**
     * Check if customer exists with given mobile number or email
     */
    private void checkCustomerExists(String mobileNumber, String email) {
        // Check for duplicate mobile number
        customerRepository.findByMobileNumber(mobileNumber)
                .ifPresent(c -> {
                    throw new ApiException(
                            "Customer with mobile number " + mobileNumber + " already exists");
                });

        // Check for duplicate email
        customerRepository.findByEmail(email)
                .ifPresent(c -> {
                    throw new ApiException(
                            "Customer with email " + email + " already exists");
                });
    }
}
