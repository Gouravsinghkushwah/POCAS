package com.pocas.service;

import com.pocas.request.AccountRequest;
import com.pocas.response.AccountResponse;

import java.util.List;

public interface AccountService {
    
    /**
     * Create a new Account
     */
    AccountResponse createAccount(AccountRequest request);
    
    /**
     * Get all accounts
     */
    List<AccountResponse> getAllAccounts();
    
    /**
     * Get account by ID
     */
    AccountResponse getAccountById(Long id);

    /**
     * Update an account
     */
    String updateAccount(Long id, AccountRequest request);
}
