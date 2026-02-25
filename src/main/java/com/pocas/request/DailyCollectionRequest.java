package com.pocas.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCollectionRequest {

    @NotNull(message = "CollectionAccount ID is required")
    private Long accountId;

    @NotNull(message = "Collection date is required")
    private LocalDate collectionDate;

    @NotNull(message = "Collected amount is required")
    @DecimalMin(value = "1.0", message = "Collected amount must be greater than 0")
    private BigDecimal collectedAmount;

    private String description;
}
