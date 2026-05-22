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
public class PropertyBondResponse {

    private Long id;
    private String userEmail;
    private String title;
    private String description;
    private BigDecimal initialAmount;
    private BigDecimal monthlyContribution;
    private Integer termMonths;
    private BigDecimal interestRate;
    private BondForecastResultDto forecastResults;
    private List<BondMonthlyProjectionDto> monthlyProjection;
}
