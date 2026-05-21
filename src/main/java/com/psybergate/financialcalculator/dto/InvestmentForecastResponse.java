package com.psybergate.financialcalculator.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentForecastResponse {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private BigDecimal initialAmount;
    private BigDecimal monthlyContribution;
    private Integer termMonths;
    private BigDecimal annualInterestRate;
    private ForecastResultDto forecastResults;
    private List<MonthlyProjectionEntryDto> monthlyProjection;
}
