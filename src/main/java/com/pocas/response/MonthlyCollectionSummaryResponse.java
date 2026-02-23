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
public class MonthlyCollectionSummaryResponse {

    private Long accountId;
    private Long customerId;
    private String customerName;
    private Integer month;
    private Integer year;
    private BigDecimal totalCollected;
    private BigDecimal monthlyKist;
}