package com.psybergate.financialcalculator.service;

import com.psybergate.financialcalculator.dto.BondMonthlyProjectionDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PropertyBondCalculator {

    public record BondCalculationResult(
            BigDecimal totalLoanAmount,
            BigDecimal totalRepayments,
            BigDecimal totalInterestPaid,
            BigDecimal remainingBalance,
            Integer estimatedPayoffMonth,
            Boolean fullyPaid,
            List<BondMonthlyProjectionDto> entries
    ) {}

    public BondCalculationResult calculate(BigDecimal initialAmount,
                                           BigDecimal monthlyContribution,
                                           Integer termMonths,
                                           BigDecimal interestRate) {

        BigDecimal monthlyRate = interestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        List<BondMonthlyProjectionDto> entries = new ArrayList<>();
        BigDecimal totalRepaymentsAccum = BigDecimal.ZERO;
        BigDecimal totalInterestAccum = BigDecimal.ZERO;
        Integer payoffMonth = null;
        BigDecimal balance = initialAmount;

        for (int m = 1; m <= termMonths; m++) {
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                entries.add(BondMonthlyProjectionDto.builder()
                        .month(m)
                        .startingBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .monthlyPayment(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .interestCharged(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .principalPaid(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .endingBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .build());
                continue;
            }

            BigDecimal startingBalance = balance;
            BigDecimal interestCharged = startingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPaid = monthlyContribution.subtract(interestCharged);
            BigDecimal actualPayment;
            BigDecimal endingBalance;

            if (principalPaid.compareTo(startingBalance) >= 0) {
                principalPaid = startingBalance;
                actualPayment = startingBalance.add(interestCharged).setScale(2, RoundingMode.HALF_UP);
                endingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                if (payoffMonth == null) payoffMonth = m;
            } else {
                actualPayment = monthlyContribution;
                endingBalance = startingBalance.subtract(principalPaid).setScale(2, RoundingMode.HALF_UP);
                if (endingBalance.compareTo(BigDecimal.ZERO) <= 0) {
                    endingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    if (payoffMonth == null) payoffMonth = m;
                }
            }

            totalRepaymentsAccum = totalRepaymentsAccum.add(actualPayment);
            totalInterestAccum = totalInterestAccum.add(interestCharged);

            entries.add(BondMonthlyProjectionDto.builder()
                    .month(m)
                    .startingBalance(startingBalance.setScale(2, RoundingMode.HALF_UP))
                    .monthlyPayment(actualPayment)
                    .interestCharged(interestCharged)
                    .principalPaid(principalPaid.setScale(2, RoundingMode.HALF_UP))
                    .endingBalance(endingBalance)
                    .build());

            balance = endingBalance;
        }

        BigDecimal remainingBalance = balance.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        int resolvedPayoffMonth = payoffMonth != null ? payoffMonth : termMonths;

        return new BondCalculationResult(
                initialAmount.setScale(2, RoundingMode.HALF_UP),
                totalRepaymentsAccum.setScale(2, RoundingMode.HALF_UP),
                totalInterestAccum.setScale(2, RoundingMode.HALF_UP),
                remainingBalance,
                resolvedPayoffMonth,
                remainingBalance.compareTo(BigDecimal.ZERO) == 0,
                entries
        );
    }
}
