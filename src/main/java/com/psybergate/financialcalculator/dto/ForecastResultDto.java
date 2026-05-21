package com.psybergate.financialcalculator.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForecastResultDto {
    private BigDecimal projectedValue;
    private BigDecimal totalContributions;
    private BigDecimal totalInterestEarned;
    private BigDecimal roiPercentage;
    private BigDecimal averageMonthlyGrowth;
}
