package com.pocas.controller;

import com.pocas.entity.CustomerStatus;
import com.pocas.request.CustomerRequest;
import com.pocas.response.CustomerResponse;
import com.pocas.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;


    @GetMapping("/")
    public String home() {
        return "API Running";
    }

    // Create customer
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Validated @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.ok(response);
    }

    // Get all customers
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    // Get customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    // Update customer
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id, @Validated @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    // Update customer status
    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerResponse> updateCustomerStatus(@PathVariable Long id, @RequestParam CustomerStatus status) {
        CustomerResponse response = customerService.updateCustomerStatus(id, status);
        return ResponseEntity.ok(response);
    }

    // Get all customers including CLOSED ones (admin view)
    @GetMapping("/all")
    public ResponseEntity<List<CustomerResponse>> getAllCustomersIncludingClosed() {
        List<CustomerResponse> customers = customerService.getAllCustomersIncludingClosed();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/closed-customers-collectionAccount")
    public ResponseEntity<List<CustomerResponse>> getAllClosedCustomer(){
        List<CustomerResponse> customers = customerService.getAllClosedCustomer();
        return ResponseEntity.ok(customers);
    }

    // Get total customers count
    @GetMapping("/total-count")
    public ResponseEntity<Map<String, Long>> getTotalCustomers() {
        long totalCustomers = customerService.getTotalCustomers();
        Map<String, Long> response = new HashMap<>();
        response.put("totalCustomers", totalCustomers);
        return ResponseEntity.ok(response);
    }
}
