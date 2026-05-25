package com.psybergate.financialcalculator.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarLoanRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal purchasePrice;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal initialDeposit;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal onceOffFee;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal adminFee;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal balloonPayment;

    @NotNull
    @Min(1)
    private Integer termMonths;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal interestRate;
}
