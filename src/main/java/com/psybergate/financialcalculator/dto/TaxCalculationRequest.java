package com.psybergate.financialcalculator.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationRequest {

    @NotNull
    private String userId;

    @NotBlank
    private String title;

    private String description;

    @DecimalMin("0.00")
    private BigDecimal salary;

    @DecimalMin("0.00")
    private BigDecimal interestIncome;

    @DecimalMin("0.00")
    private BigDecimal dividend;

    @DecimalMin("0.00")
    private BigDecimal capitalGain;

    @DecimalMin("0.00")
    private BigDecimal bonus;

    @DecimalMin("0.00")
    private BigDecimal retirementAnnuity;

    @NotNull
    @Min(0)
    private Integer age;

    @DecimalMin("0.00")
    private BigDecimal taxAlreadyPaid;
}
