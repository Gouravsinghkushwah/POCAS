package com.pocas.service;

import com.pocas.request.DailyCollectionRequest;
import com.pocas.response.*;

import java.util.List;

public interface DailyCollectionService {
    
    /**
     * Add a daily collection entry
     */
    DailyCollectionResponse addDailyCollection(DailyCollectionRequest request);
    
    /**
     * Get all collections for account
     */
    List<DailyCollectionResponse> getAllCollections(Long accountId);
    
    /**
     * Get monthly summary
     */
    MonthlyCollectionSummaryResponse getMonthlySummary(Long accountId, Integer month, Integer year);
    
    /**
     * Get remaining and collected amounts for a month
     */
    MonthlyAccountSummaryResponse getRemainingAndCollected(Long accountId, Integer month, Integer year);
    
    /**
     * Get all collections with full details
     */
    List<DailyCollectionFullResponse> getAllCollectionsFull();
    
    /**
     * Get collections by customer ID
     */
    List<DailyCollectionFullResponse> getCollectionsByCustomerId(Long customerId);
    
    /**
     * Get payment status by account ID
     */
    List<DailyPaymentStatusResponse> getPaymentStatusByAccountId(Long accountId);
}
