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
public class MonthlyPaymentSummaryResponse {

    private Long accountId;
    private Integer month;
    private Integer year;
    private Integer totalDays;
    private Integer paidDays;
    private Integer unpaidDays;
    private BigDecimal expectedAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal remainingAmount;
}
