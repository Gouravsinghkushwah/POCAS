package com.pocas.response;

import com.pocas.entity.AccountType;
import com.pocas.entity.CustomerStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;
    private String name;
    private String mobileNumber;
    private String email;
    private String address;
    private AccountType accountType;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}