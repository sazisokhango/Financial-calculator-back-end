package com.psybergate.financialcalculator.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyProjectionEntryDto {
    private Integer month;
    private BigDecimal startingBalance;
    private BigDecimal monthlyContribution;
    private BigDecimal interestEarned;
    private BigDecimal endingBalance;
}
