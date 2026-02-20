package com.pocas.serviceimpl;

import com.pocas.constants.ApiMessages;
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
            throw new ApiException(ApiMessages.FAILED_TO_CREATE_CUSTOMER);
        }
    }

    /**
     * Get all customers
     */
    @Override
    public List<CustomerResponse> getAllCustomers() {
        try {
            return customerRepository.findAll().stream()
                    .filter(customer -> customer.getStatus() != CustomerStatus.CLOSED)
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException(ApiMessages.FAILED_TO_FETCH_CUSTOMERS);
        }
    }

    @Override
    public List<CustomerResponse> getAllClosedCustomer(){
        try{
            return customerRepository.findAll().stream()
                    .filter(customer -> customer.getStatus()==CustomerStatus.CLOSED)
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }catch(Exception e){
            throw new ApiException(ApiMessages.FAILED_TO_FETCH_CUSTOMERS);
        }
    }

    /**
     * Get customer by ID
     */
    @Override
    public CustomerResponse getCustomerById(Long id) {
        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ApiException(String.format(ApiMessages.CUSTOMER_NOT_FOUND, id)));
            return mapToResponse(customer);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ApiMessages.FAILED_TO_FETCH_CUSTOMERS);
        }
    }
    
    
    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ApiException(String.format(ApiMessages.CUSTOMER_NOT_FOUND, id)));

            // Check if customer is active
            ensureCustomerActive(customer);

            checkCustomerExistsForUpdate(id, request.getMobileNumber(), request.getEmail());
            // Update customer fields
            customer.setName(request.getName());
            customer.setMobileNumber(request.getMobileNumber());
            customer.setEmail(request.getEmail());
            customer.setAddress(request.getAddress());
            customer.setAccountType(request.getAccountType());
            
            Customer saved = customerRepository.save(customer);
            return mapToResponse(saved);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ApiMessages.FAILED_TO_UPDATE_CUSTOMER);
        }
    }
    
    @Override
    public CustomerResponse updateCustomerStatus(Long id, CustomerStatus status) {
        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ApiException(String.format(ApiMessages.CUSTOMER_NOT_FOUND, id)));
            
            // Check if customer already has the same status
            if (customer.getStatus() == status) {
                // Return current customer without error
                return mapToResponse(customer);
            }
            
            // Update customer status
            customer.setStatus(status);
            
            Customer saved = customerRepository.save(customer);
            return mapToResponse(saved);
            
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ApiMessages.FAILED_TO_UPDATE_CUSTOMER_STATUS);
        }
    }
    
    @Override
    public List<CustomerResponse> getAllCustomersIncludingClosed() {
        try {
            return customerRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException(ApiMessages.FAILED_TO_FETCH_CUSTOMERS);
        }
    }

    /**
     * Check if customer exists with given mobile number or email
     */
    private void checkCustomerExists(String mobileNumber, String email) {
        checkMobileNumberExists(mobileNumber);
        checkEmailExists(email);
    }

    /**
     * Check if mobile number exists (excluding CLOSED customers)
     */
    private void checkMobileNumberExists(String mobileNumber) {
        customerRepository.findByMobileNumber(mobileNumber)
                .ifPresent(c -> {
                    if (c.getStatus() != CustomerStatus.CLOSED) {
                        throw new ApiException(
                                String.format(ApiMessages.CUSTOMER_MOBILE_EXISTS, mobileNumber));
                    }
                });
    }

    /**
     * Check if email exists (excluding CLOSED customers)
     */
    private void checkEmailExists(String email) {
        customerRepository.findByEmail(email)
                .ifPresent(c -> {
                    if (c.getStatus() != CustomerStatus.CLOSED) {
                        throw new ApiException(
                                String.format(ApiMessages.CUSTOMER_EMAIL_EXISTS, email));
                    }
                });
    }

    /**
     * Check if customer is active (not CLOSED)
     */
    private void ensureCustomerActive(Customer customer) {
        if (customer.getStatus() == CustomerStatus.CLOSED) {
            throw new ApiException(ApiMessages.CUSTOMER_DEACTIVATED);
        }
    }

    /**
     * Check if customer exists with given mobile number or email (excluding current customer)
     */
    private void checkCustomerExistsForUpdate(Long currentCustomerId, String mobileNumber, String email) {
        // Check for duplicate mobile number (excluding current customer)
        customerRepository.findByMobileNumber(mobileNumber)
                .ifPresent(c -> {
                    if (!c.getId().equals(currentCustomerId)) {
                        throw new ApiException(
                                String.format(ApiMessages.CUSTOMER_MOBILE_EXISTS, mobileNumber));
                    }
                });

        // Check for duplicate email (excluding current customer)
        customerRepository.findByEmail(email)
                .ifPresent(c -> {
                    if (!c.getId().equals(currentCustomerId)) {
                        throw new ApiException(
                                String.format(ApiMessages.CUSTOMER_EMAIL_EXISTS, email));
                    }
                });
    }

    @Override
    public long getTotalCustomers() {
        return customerRepository.countByStatusNot(CustomerStatus.CLOSED);
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

}
