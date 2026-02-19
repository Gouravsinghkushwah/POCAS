package com.pocas.controller;

import com.pocas.request.CollectionAccountRequest;
import com.pocas.response.CollectionAccountResponse;
import com.pocas.response.ApiResponseDto;
import com.pocas.service.CollectionAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class CollectionAccountController {

    private final CollectionAccountService collectionAccountService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<CollectionAccountResponse>> createAccount(
            @Valid @RequestBody CollectionAccountRequest request) {
        CollectionAccountResponse response = collectionAccountService.createAccount(request);
        ApiResponseDto<CollectionAccountResponse> apiResponse = ApiResponseDto.<CollectionAccountResponse>builder()
                .message("CollectionAccount created successfully")
                .data(response)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<CollectionAccountResponse>>> getAllAccounts() {
        List<CollectionAccountResponse> accounts = collectionAccountService.getAllAccounts();
        ApiResponseDto<List<CollectionAccountResponse>> apiResponse = ApiResponseDto.<List<CollectionAccountResponse>>builder()
                .message("Accounts fetched successfully")
                .data(accounts)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CollectionAccountResponse>> getAccountById(@PathVariable Long id) {
        CollectionAccountResponse response = collectionAccountService.getAccountById(id);
        ApiResponseDto<CollectionAccountResponse> apiResponse = ApiResponseDto.<CollectionAccountResponse>builder()
                .message("CollectionAccount fetched successfully")
                .data(response)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateAccount(@PathVariable Long id, @Valid @RequestBody CollectionAccountRequest request) {
        collectionAccountService.updateAccount(id, request);
        return ResponseEntity.ok("CollectionAccount updated successfully");
    }
}