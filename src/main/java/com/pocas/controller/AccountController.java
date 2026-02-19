package com.pocas.controller;

import com.pocas.request.AccountRequest;
import com.pocas.response.AccountResponse;
import com.pocas.response.ApiResponseDto;
import com.pocas.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<AccountResponse>> createAccount(
            @Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        ApiResponseDto<AccountResponse> apiResponse = ApiResponseDto.<AccountResponse>builder()
                .message("Account created successfully")
                .data(response)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<AccountResponse>>> getAllAccounts() {
        List<AccountResponse> accounts = accountService.getAllAccounts();
        ApiResponseDto<List<AccountResponse>> apiResponse = ApiResponseDto.<List<AccountResponse>>builder()
                .message("Accounts fetched successfully")
                .data(accounts)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<AccountResponse>> getAccountById(@PathVariable Long id) {
        AccountResponse response = accountService.getAccountById(id);
        ApiResponseDto<AccountResponse> apiResponse = ApiResponseDto.<AccountResponse>builder()
                .message("Account fetched successfully")
                .data(response)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountRequest request) {
        accountService.updateAccount(id, request);
        return ResponseEntity.ok("Account updated successfully");
    }
}