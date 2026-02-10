package com.pocas.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyAccountSummaryResponse {

    private Long accountId;
    private Long customerId;
    private String customerName;

    private Integer month;
    private Integer year;

    private BigDecimal monthlyKist;       // Expected monthly amount
    private BigDecimal collectedThisMonth; // Total collected till this month
    private BigDecimal remainingMonthsAmount; // Remaining amount including unpaid months
    private BigDecimal remainingAmount;    // Remaining amount including unpaid months
    private Integer remainingMonths;       // Remaining months to complete
}