package com.pocas.request;

import com.pocas.entity.AccountType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionAccountRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "CollectionAccount type is required")
    private AccountType accountType;

    @NotNull(message = "Monthly Kist amount is required")
    @DecimalMin(value = "1.0", message = "Monthly Kist must be greater than 0")
    private BigDecimal monthlyKist;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
}