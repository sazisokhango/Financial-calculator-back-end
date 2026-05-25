package com.psybergate.financialcalculator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarLoanForecastResultDto {

    private BigDecimal financedAmount;
    private BigDecimal monthlyRepayment;
    private BigDecimal totalRepayments;
    private BigDecimal totalInterestPaid;
    private BigDecimal totalFeesPaid;
    private BigDecimal balloonPayment;
    private BigDecimal remainingBalance;
    private Integer estimatedPayoffMonth;
    private Boolean fullyPaid;
}
