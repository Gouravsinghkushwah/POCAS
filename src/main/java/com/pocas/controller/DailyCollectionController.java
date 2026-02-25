package com.pocas.controller;

import com.pocas.request.DailyCollectionRequest;
import com.pocas.response.*;
import com.pocas.service.DailyCollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/daily-collections")
@RequiredArgsConstructor
public class DailyCollectionController {

    private final DailyCollectionService dailyCollectionService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<DailyCollectionResponse>> addDailyCollection(
            @Valid @RequestBody DailyCollectionRequest request) {
        DailyCollectionResponse response = dailyCollectionService.addDailyCollection(request);
        ApiResponseDto<DailyCollectionResponse> apiResponse = ApiResponseDto.<DailyCollectionResponse>builder()
                .message("Daily collection added successfully")
                .data(response)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/collectionAccount/{accountId}")
    public ResponseEntity<ApiResponseDto<List<DailyCollectionResponse>>> getAllCollections(
            @PathVariable Long accountId) {
        List<DailyCollectionResponse> collections = dailyCollectionService.getAllCollections(accountId);
        ApiResponseDto<List<DailyCollectionResponse>> apiResponse = ApiResponseDto.<List<DailyCollectionResponse>>builder()
                .message("Daily collections fetched successfully")
                .data(collections)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/collectionAccount/{accountId}/month-summary")
    public ResponseEntity<ApiResponseDto<MonthlyCollectionSummaryResponse>> getMonthlySummary(
            @PathVariable Long accountId,
            @RequestParam Integer month,
            @RequestParam Integer year) {

        MonthlyCollectionSummaryResponse response =
                dailyCollectionService.getMonthlySummary(accountId, month, year);

        ApiResponseDto<MonthlyCollectionSummaryResponse> apiResponse =
                ApiResponseDto.<MonthlyCollectionSummaryResponse>builder()
                        .message("Monthly summary fetched successfully")
                        .data(response)
                        .status(200)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/collectionAccount/{accountId}/month-summary-detailed")
    public ResponseEntity<ApiResponseDto<MonthlyAccountSummaryResponse>> getRemainingAndCollected(
            @PathVariable Long accountId,
            @RequestParam Integer month,
            @RequestParam Integer year) {

        MonthlyAccountSummaryResponse response =
                dailyCollectionService.getRemainingAndCollected(accountId, month, year);

        ApiResponseDto<MonthlyAccountSummaryResponse> apiResponse =
                ApiResponseDto.<MonthlyAccountSummaryResponse>builder()
                        .message("Monthly collected and remaining data fetched successfully")
                        .data(response)
                        .status(200)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponseDto<List<DailyCollectionFullResponse>>> getAllCollectionsFull() {
        List<DailyCollectionFullResponse> collections = dailyCollectionService.getAllCollectionsFull();

        ApiResponseDto<List<DailyCollectionFullResponse>> apiResponse =
                ApiResponseDto.<List<DailyCollectionFullResponse>>builder()
                        .message("All daily collections fetched successfully")
                        .data(collections)
                        .status(200)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponseDto<List<DailyCollectionFullResponse>>> getCollectionsByCustomerId(
            @PathVariable Long customerId) {

        List<DailyCollectionFullResponse> collections =
                dailyCollectionService.getCollectionsByCustomerId(customerId);

        ApiResponseDto<List<DailyCollectionFullResponse>> apiResponse =
                ApiResponseDto.<List<DailyCollectionFullResponse>>builder()
                        .message("Collections fetched successfully for customer ID " + customerId)
                        .data(collections)
                        .status(200)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/collectionAccount/{accountId}/payment-status")
    public ResponseEntity<ApiResponseDto<List<DailyPaymentStatusResponse>>> getPaymentStatus(
            @PathVariable Long accountId) {

        List<DailyPaymentStatusResponse> statusList = dailyCollectionService.getPaymentStatusByAccountId(accountId);

        ApiResponseDto<List<DailyPaymentStatusResponse>> apiResponse =
                ApiResponseDto.<List<DailyPaymentStatusResponse>>builder()
                        .message("Daily payment status fetched successfully")
                        .data(statusList)
                        .status(200)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<DailyCollectionFullResponse>> getAllCollectionsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Page<DailyCollectionFullResponse> collectionsPage = 
                dailyCollectionService.getAllCollectionsPaginated(page, size, search);

        return ResponseEntity.ok(collectionsPage);
    }

    @GetMapping("/latest-per-account")
    public ResponseEntity<ApiResponseDto<List<DailyCollectionFullResponse>>> getLatestCollectionPerAccount() {
        List<DailyCollectionFullResponse> collections = dailyCollectionService.getLatestCollectionPerAccount();

        ApiResponseDto<List<DailyCollectionFullResponse>> apiResponse =
                ApiResponseDto.<List<DailyCollectionFullResponse>>builder()
                        .message("Latest collection per account fetched successfully")
                        .data(collections)
                        .status(200)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/collectionAccount/{accountId}/transactions")
    public ResponseEntity<ApiResponseDto<List<DailyCollectionFullResponse>>> getAllTransactionsByAccountId(
            @PathVariable Long accountId) {

        List<DailyCollectionFullResponse> collections =
                dailyCollectionService.getAllTransactionsByAccountId(accountId);

        ApiResponseDto<List<DailyCollectionFullResponse>> apiResponse =
                ApiResponseDto.<List<DailyCollectionFullResponse>>builder()
                        .message("All transactions fetched successfully for account ID " + accountId)
                        .data(collections)
                        .status(200)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

}
