package com.pocas.serviceimpl;

import com.pocas.entity.AccountType;
import com.pocas.entity.CollectionAccount;
import com.pocas.entity.AccountStatus;
import com.pocas.entity.Customer;
import com.pocas.entity.CustomerStatus;
import com.pocas.exception.ApiException;
import com.pocas.repo.AccountRepository;
import com.pocas.repo.CustomerRepository;
import com.pocas.request.CollectionAccountRequest;
import com.pocas.response.CollectionAccountResponse;
import com.pocas.service.CollectionAccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionAccountServiceImpl implements CollectionAccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final Logger logger = LoggerFactory.getLogger(CollectionAccountServiceImpl.class);

    /**
     * Create a new CollectionAccount
     */
    @Override
    public CollectionAccountResponse createAccount(CollectionAccountRequest request) {
        try {
            // Fetch customer
            Customer customer = getCustomerById(request.getCustomerId());

            // Check if customer is active
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException("Cannot create account for deactivated customer. Customer status: " + customer.getStatus());
            }

            // Check if customer already has active collectionAccount
            checkCustomerHasActiveAccount(customer);

            // Calculate total months
            int totalMonths = request.getAccountType() == AccountType.FIVE_YEARS ? 36 : 60;

            // Calculate end date
            LocalDate endDate = request.getStartDate().plusMonths(totalMonths);

            // Calculate total expected deposit
            BigDecimal totalDeposit = request.getMonthlyKist().multiply(BigDecimal.valueOf(totalMonths));

            // Generate system collectionAccount number (ACC + timestamp)
            String accountNumber = "ACC" + System.currentTimeMillis();

            // Build collectionAccount entity
            CollectionAccount collectionAccount = CollectionAccount.builder()
                    .accountNumber(accountNumber)
                    .customer(customer)
                    .accountType(request.getAccountType())
                    .monthlyKist(request.getMonthlyKist())
                    .startDate(request.getStartDate())
                    .endDate(endDate)
                    .totalMonths(totalMonths)
                    .remainingMonths(totalMonths)
                    .totalExpectedDeposit(totalDeposit)
                    .status(AccountStatus.ACTIVE)
                    .build();

            CollectionAccount saved = accountRepository.save(collectionAccount);

            return mapToResponse(saved);

        } catch (ApiException e) {
            throw e; // propagate for global handler
        } catch (Exception e) {
            throw new ApiException("Failed to create collectionAccount. Please try again later.");
        }
    }

    /**
     * Get all accounts
     */
    @Override
    public List<CollectionAccountResponse> getAllAccounts() {
        try {
            return accountRepository.findAll().stream()
                    .filter(account -> account.getCustomer().getStatus() != CustomerStatus.CLOSED)
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException("Failed to fetch accounts. Please try again later.");
        }
    }

    /**
     * Get collectionAccount by ID
     */
    @Override
    public CollectionAccountResponse getAccountById(Long id) {
        try {
            CollectionAccount collectionAccount = accountRepository.findById(id)
                    .orElseThrow(() -> new ApiException("CollectionAccount not found with ID " + id));
            
            // Check if customer is active
            Customer customer = collectionAccount.getCustomer();
            if (customer.getStatus() == CustomerStatus.CLOSED) {
                throw new ApiException("Cannot view account for deactivated customer. Customer status: " + customer.getStatus());
            }
            
            return mapToResponse(collectionAccount);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to fetch collectionAccount. Please try again later.");
        }
    }

    /**
     * Update an collectionAccount
     */
    @Override
    public String  updateAccount(Long id, CollectionAccountRequest request) {
        CollectionAccount collectionAccount = getCustomerAccountById(id);
        
        // Check if customer is active
        Customer customer = collectionAccount.getCustomer();
        if (customer.getStatus() == CustomerStatus.CLOSED) {
            throw new ApiException("Cannot update account for deactivated customer. Customer status: " + customer.getStatus());
        }
        
        // Update fields
        collectionAccount.setAccountType(request.getAccountType());
        collectionAccount.setMonthlyKist(request.getMonthlyKist());
        collectionAccount.setStartDate(request.getStartDate());
        
        // Recalculate end date and total months
        int totalMonths = request.getAccountType() == AccountType.FIVE_YEARS ? 36 : 60;
        LocalDate endDate = request.getStartDate().plusMonths(totalMonths);
        BigDecimal totalDeposit = request.getMonthlyKist().multiply(BigDecimal.valueOf(totalMonths));
        
        collectionAccount.setEndDate(endDate);
        collectionAccount.setTotalMonths(totalMonths);
        collectionAccount.setRemainingMonths(totalMonths);
        collectionAccount.setTotalExpectedDeposit(totalDeposit);
        
        accountRepository.save(collectionAccount);
        return "CollectionAccount updated successfully";
    }

    /**
     * Helper method to map Entity → Response DTO
     */
    private CollectionAccountResponse mapToResponse(CollectionAccount collectionAccount) {
        if (collectionAccount == null) return null;
        return CollectionAccountResponse.builder()
                .id(collectionAccount.getId())
                .accountNumber(collectionAccount.getAccountNumber())
                .customerId(collectionAccount.getCustomer().getId())
                .accountType(collectionAccount.getAccountType())
                .monthlyKist(collectionAccount.getMonthlyKist())
                .startDate(collectionAccount.getStartDate())
                .endDate(collectionAccount.getEndDate())
                .totalMonths(collectionAccount.getTotalMonths())
                .remainingMonths(collectionAccount.getRemainingMonths())
                .totalExpectedDeposit(collectionAccount.getTotalExpectedDeposit())
                .status(collectionAccount.getStatus())
                .createdAt(collectionAccount.getCreatedAt())
                .updatedAt(collectionAccount.getUpdatedAt())
                .build();
    }

    public void checkCustomerHasActiveAccount(Customer customer) {
        accountRepository.findByCustomer(customer).stream()
                .filter(acc -> acc.getStatus() == AccountStatus.ACTIVE)
                .findAny()
                .ifPresent(acc -> {
                    throw new ApiException("Customer already has an active collectionAccount");
                });
    }

    public CollectionAccount getCustomerAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ApiException("CollectionAccount not found with ID " + id));
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ApiException("Customer not found with ID " + id));
    }
}
