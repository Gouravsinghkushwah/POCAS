package com.pocas.response;

import com.pocas.entity.AccountStatus;
import com.pocas.entity.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSearchResponse {

    private Long id;
    private String accountNumber;
    private String customerName;
    private String mobileNumber;
    private String email;
    private AccountType accountType;
    private BigDecimal monthlyKist;
    private LocalDate startDate;
    private LocalDate endDate;
    private AccountStatus status;
}
