package com.pocas.service;

import com.pocas.request.CollectionAccountRequest;
import com.pocas.response.CollectionAccountResponse;

import java.util.List;

public interface CollectionAccountService {
    
    /**
     * Create a new CollectionAccount
     */
    CollectionAccountResponse createAccount(CollectionAccountRequest request);
    
    /**
     * Get all accounts
     */
    List<CollectionAccountResponse> getAllAccounts();
    
    /**
     * Get collectionAccount by ID
     */
    CollectionAccountResponse getAccountById(Long id);

    /**
     * Update an collectionAccount
     */
    String updateAccount(Long id, CollectionAccountRequest request);
}
