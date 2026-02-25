package com.pocas.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    
    // Advance payment fields
    private BigDecimal advanceFromPreviousMonths;
    private BigDecimal currentMonthAdvance;
    private BigDecimal remainingAdvance;
    private BigDecimal adjustedExpectedAmount;
    private Integer monthsCoveredByAdvance;
    private List<AdvancePaymentDetail> advanceDetails;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdvancePaymentDetail {
        private Integer originalMonth;
        private Integer originalYear;
        private BigDecimal advanceAmount;
        private BigDecimal remainingAdvance;
        private BigDecimal appliedThisMonth;
    }
}
