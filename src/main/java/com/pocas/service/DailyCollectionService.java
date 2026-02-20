package com.pocas.service;

import com.pocas.request.DailyCollectionRequest;
import com.pocas.response.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DailyCollectionService {
    
    /**
     * Add a daily collection entry
     */
    DailyCollectionResponse addDailyCollection(DailyCollectionRequest request);
    
    /**
     * Get all collections for collectionAccount
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
     * Get payment status by collectionAccount ID
     */
    List<DailyPaymentStatusResponse> getPaymentStatusByAccountId(Long accountId);
    
    /**
     * Get monthly payment summary for an account
     */
    MonthlyPaymentSummaryResponse getMonthlyPaymentSummary(Long accountId, Integer month, Integer year);
    
    /**
     * Get all collections with pagination and search
     */
    Page<DailyCollectionFullResponse> getAllCollectionsPaginated(int page, int size, String search);
}
