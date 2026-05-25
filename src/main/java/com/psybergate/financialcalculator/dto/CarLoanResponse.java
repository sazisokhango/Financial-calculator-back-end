package com.psybergate.financialcalculator.dto;

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
public class CarLoanResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal purchasePrice;
    private BigDecimal initialDeposit;
    private BigDecimal onceOffFee;
    private BigDecimal adminFee;
    private BigDecimal balloonPayment;
    private Integer termMonths;
    private BigDecimal interestRate;
    private CarLoanForecastResultDto forecastResults;
    private List<CarLoanMonthlyProjectionDto> monthlyProjection;
}
