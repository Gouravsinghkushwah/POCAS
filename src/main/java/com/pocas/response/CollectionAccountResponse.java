package com.pocas.response;

import com.pocas.entity.AccountStatus;
import com.pocas.entity.AccountType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionAccountResponse {

    private Long id;
    private String accountNumber;
    private String customerName;
    private Long customerId;
    private AccountType accountType;
    private BigDecimal monthlyKist;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalMonths;
    private Integer remainingMonths;
    private BigDecimal totalExpectedDeposit;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
