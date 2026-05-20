package com.psybergate.financialcalculator.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxCalculationResponse {

    private Long id;
    private Long userId;
    private String title;
    private String description;

    // Inputs
    private BigDecimal salary;
    private BigDecimal interestIncome;
    private BigDecimal dividend;
    private BigDecimal capitalGain;
    private BigDecimal bonus;
    private BigDecimal retirementAnnuity;
    private Integer age;
    private BigDecimal taxAlreadyPaid;

    // Computed breakdown
    private BigDecimal totalIncome;
    private BigDecimal totalDeductions;
    private BigDecimal netTaxableIncome;
    private BigDecimal taxBeforeRebate;
    private BigDecimal rebate;
    private BigDecimal finalTaxLiability;
}
