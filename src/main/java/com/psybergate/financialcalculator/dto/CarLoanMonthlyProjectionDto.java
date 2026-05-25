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
public class CarLoanMonthlyProjectionDto {

    private Integer month;
    private BigDecimal startingBalance;
    private BigDecimal monthlyRepayment;
    private BigDecimal interestCharged;
    private BigDecimal adminFee;
    private BigDecimal principalPaid;
    private BigDecimal endingBalance;
}
