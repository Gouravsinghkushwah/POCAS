package com.pocas.response;

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
public class DailyCollectionFullResponse {

    private Long collectionId;
    private Long accountId;
    private Long customerId;
    private String customerName;
    private LocalDate collectionDate;
    private BigDecimal collectedAmount;
    private Integer month;
    private Integer year;
}