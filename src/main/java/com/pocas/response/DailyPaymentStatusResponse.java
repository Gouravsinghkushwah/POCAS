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
public class DailyPaymentStatusResponse {

    private LocalDate date;
    private BigDecimal paidAmount; // 0 if missed
    private boolean isPaid;
}