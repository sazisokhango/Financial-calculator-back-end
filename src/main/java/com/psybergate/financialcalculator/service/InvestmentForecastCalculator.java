package com.psybergate.financialcalculator.service;

import com.psybergate.financialcalculator.dto.MonthlyProjectionEntryDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvestmentForecastCalculator {

    public record ForecastCalculationResult(
            BigDecimal projectedValue,
            BigDecimal totalContributions,
            BigDecimal totalInterestEarned,
            BigDecimal roiPercentage,
            BigDecimal averageMonthlyGrowth,
            List<MonthlyProjectionEntryDto> entries
    ) {}

    public ForecastCalculationResult calculate(BigDecimal initialAmount,
                                               BigDecimal monthlyContribution,
                                               Integer termMonths,
                                               BigDecimal annualInterestRate) {
        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        List<MonthlyProjectionEntryDto> entries = new ArrayList<>();
        BigDecimal balance = initialAmount;

        for (int m = 1; m <= termMonths; m++) {
            BigDecimal startingBalance = balance;
            BigDecimal interestEarned = startingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal endingBalance = startingBalance.add(monthlyContribution).add(interestEarned).setScale(2, RoundingMode.HALF_UP);

            entries.add(MonthlyProjectionEntryDto.builder()
                    .month(m)
                    .startingBalance(startingBalance)
                    .monthlyContribution(monthlyContribution)
                    .interestEarned(interestEarned)
                    .endingBalance(endingBalance)
                    .build());

            balance = endingBalance;
        }

        BigDecimal projectedValue = balance;
        BigDecimal totalContributions = initialAmount
                .add(monthlyContribution.multiply(BigDecimal.valueOf(termMonths)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalInterestEarned = projectedValue.subtract(totalContributions).setScale(2, RoundingMode.HALF_UP);

        BigDecimal roiPercentage = totalContributions.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : totalInterestEarned.divide(totalContributions, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal averageMonthlyGrowth = termMonths == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : totalInterestEarned.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);

        return new ForecastCalculationResult(
                projectedValue, totalContributions, totalInterestEarned,
                roiPercentage, averageMonthlyGrowth, entries
        );
    }
}
