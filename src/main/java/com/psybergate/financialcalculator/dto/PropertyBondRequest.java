package com.psybergate.financialcalculator.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyBondRequest {

    @NotBlank
    @Email
    private String userEmail;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal initialAmount;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal monthlyContribution;

    @NotNull
    @Min(1)
    private Integer termMonths;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal interestRate;
}
