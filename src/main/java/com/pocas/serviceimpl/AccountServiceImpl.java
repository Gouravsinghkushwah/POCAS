package com.pocas.serviceimpl;

import com.pocas.entity.Account;
import com.pocas.entity.AccountStatus;
import com.pocas.entity.AccountType;
import com.pocas.entity.Customer;
import com.pocas.exception.ApiException;
import com.pocas.repo.AccountRepository;
import com.pocas.repo.CustomerRepository;
import com.pocas.request.AccountRequest;
import com.pocas.response.AccountResponse;
import com.pocas.service.AccountService;
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
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    /**
     * Create a new Account
     */
    @Override
    public AccountResponse createAccount(AccountRequest request) {
        try {
            // Fetch customer
            Customer customer = getCustomerById(request.getCustomerId());

            // Check if customer already has active account
            checkCustomerHasActiveAccount(customer);

            // Calculate total months
            int totalMonths = request.getAccountType() == getAccountById(request.getCustomerId()).getAccountType() ? 36 : 60;

            // Calculate end date
            LocalDate endDate = request.getStartDate().plusMonths(totalMonths);

            // Calculate total expected deposit
            BigDecimal totalDeposit = request.getMonthlyKist().multiply(BigDecimal.valueOf(totalMonths));

            // Generate system account number (ACC + timestamp)
            String accountNumber = "ACC" + System.currentTimeMillis();

            // Build account entity
            Account account = Account.builder()
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

            Account saved = accountRepository.save(account);

            return mapToResponse(saved);

        } catch (ApiException e) {
            throw e; // propagate for global handler
        } catch (Exception e) {
            throw new ApiException("Failed to create account. Please try again later.");
        }
    }

    /**
     * Get all accounts
     */
    @Override
    public List<AccountResponse> getAllAccounts() {
        try {
            return accountRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException("Failed to fetch accounts. Please try again later.");
        }
    }

    /**
     * Get account by ID
     */
    @Override
    public AccountResponse getAccountById(Long id) {
        try {
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new ApiException("Account not found with ID " + id));
            return mapToResponse(account);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to fetch account. Please try again later.");
        }
    }

    /**
     * Update an account
     */
    @Override
    public String  updateAccount(Long id, AccountRequest request) {
        Account customerAccount = getCustomerAccountById(id);
        customerAccount.setId(id);
        accountRepository.save(customerAccount);
        return "Account updated successfully";
    }

    /**
     * Helper method to map Entity → Response DTO
     */
    private AccountResponse mapToResponse(Account account) {
        if (account == null) return null;
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomer().getId())
                .accountType(account.getAccountType())
                .monthlyKist(account.getMonthlyKist())
                .startDate(account.getStartDate())
                .endDate(account.getEndDate())
                .totalMonths(account.getTotalMonths())
                .remainingMonths(account.getRemainingMonths())
                .totalExpectedDeposit(account.getTotalExpectedDeposit())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public void checkCustomerHasActiveAccount(Customer customer) {
        accountRepository.findByCustomer(customer).stream()
                .filter(acc -> acc.getStatus() == AccountStatus.ACTIVE)
                .findAny()
                .ifPresent(acc -> {
                    throw new ApiException("Customer already has an active account");
                });
    }

    public Account getCustomerAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ApiException("Account not found with ID " + id));
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ApiException("Customer not found with ID " + id));
    }
}
