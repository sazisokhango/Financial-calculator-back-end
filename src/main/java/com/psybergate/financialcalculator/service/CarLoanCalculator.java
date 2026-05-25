package com.psybergate.financialcalculator.service;

import com.psybergate.financialcalculator.dto.CarLoanMonthlyProjectionDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarLoanCalculator {

    private static final int CALC_SCALE = 10;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public record CarLoanResult(
            BigDecimal financedAmount,
            BigDecimal monthlyRepayment,
            BigDecimal totalRepayments,
            BigDecimal totalInterestPaid,
            BigDecimal totalFeesPaid,
            BigDecimal remainingBalance,
            Integer estimatedPayoffMonth,
            Boolean fullyPaid,
            List<CarLoanMonthlyProjectionDto> monthlyProjection
    ) {}

    public CarLoanResult calculate(BigDecimal purchasePrice,
                                   BigDecimal initialDeposit,
                                   BigDecimal onceOffFee,
                                   BigDecimal adminFee,
                                   BigDecimal balloonPayment,
                                   Integer termMonths,
                                   BigDecimal interestRate) {

        BigDecimal financedAmount = purchasePrice
                .subtract(initialDeposit)
                .add(onceOffFee)
                .setScale(2, ROUNDING);

        if (financedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return zeroResult(financedAmount, balloonPayment, adminFee, termMonths);
        }

        BigDecimal monthlyRate = interestRate
                .divide(BigDecimal.valueOf(12), CALC_SCALE, ROUNDING)
                .divide(BigDecimal.valueOf(100), CALC_SCALE, ROUNDING);

        BigDecimal pmt = computePmt(financedAmount, balloonPayment, monthlyRate, termMonths);

        if (pmt.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Monthly repayment amount is too low to reduce the loan balance.");
        }

        BigDecimal totalMonthlyRepayment = pmt.add(adminFee).setScale(2, ROUNDING);

        List<CarLoanMonthlyProjectionDto> schedule = new ArrayList<>();
        BigDecimal balance = financedAmount;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalRepayments = BigDecimal.ZERO;
        Integer payoffMonth = null;

        for (int m = 1; m <= termMonths; m++) {
            BigDecimal startingBalance = balance.setScale(2, ROUNDING);
            BigDecimal interestCharged = startingBalance.multiply(monthlyRate).setScale(2, ROUNDING);

            boolean isFinalMonth = (m == termMonths);
            BigDecimal principalPaid;
            BigDecimal actualRepayment;
            BigDecimal endingBalance;

            // Final month or early payoff: absorb rounding by paying exact remaining balance
            boolean earlyPayoff = pmt.subtract(interestCharged).compareTo(startingBalance) >= 0;
            if (isFinalMonth || earlyPayoff) {
                principalPaid = startingBalance;
                actualRepayment = startingBalance.add(interestCharged).add(adminFee).setScale(2, ROUNDING);
                endingBalance = BigDecimal.ZERO.setScale(2, ROUNDING);
                if (payoffMonth == null) payoffMonth = m;
            } else {
                principalPaid = pmt.subtract(interestCharged).setScale(2, ROUNDING);
                actualRepayment = totalMonthlyRepayment;
                endingBalance = startingBalance.subtract(principalPaid).setScale(2, ROUNDING);
            }

            if (isFinalMonth && balloonPayment.compareTo(BigDecimal.ZERO) > 0) {
                endingBalance = endingBalance.subtract(balloonPayment).max(BigDecimal.ZERO).setScale(2, ROUNDING);
                if (endingBalance.compareTo(BigDecimal.ZERO) == 0 && payoffMonth == null) payoffMonth = m;
            }

            totalInterest = totalInterest.add(interestCharged);
            totalRepayments = totalRepayments.add(actualRepayment);

            schedule.add(CarLoanMonthlyProjectionDto.builder()
                    .month(m)
                    .startingBalance(startingBalance)
                    .monthlyRepayment(actualRepayment)
                    .interestCharged(interestCharged)
                    .adminFee(adminFee.setScale(2, ROUNDING))
                    .principalPaid(principalPaid)
                    .endingBalance(endingBalance)
                    .build());

            if (endingBalance.compareTo(BigDecimal.ZERO) == 0 && payoffMonth == null) {
                payoffMonth = m;
            }

            balance = endingBalance;
        }

        BigDecimal remainingBalance = balance.setScale(2, ROUNDING);
        int resolvedPayoffMonth = payoffMonth != null ? payoffMonth : termMonths;
        BigDecimal totalFeesPaid = adminFee.multiply(BigDecimal.valueOf(termMonths)).setScale(2, ROUNDING);

        return new CarLoanResult(
                financedAmount,
                totalMonthlyRepayment,
                totalRepayments.setScale(2, ROUNDING),
                totalInterest.setScale(2, ROUNDING),
                totalFeesPaid,
                remainingBalance,
                resolvedPayoffMonth,
                remainingBalance.compareTo(BigDecimal.ZERO) == 0,
                schedule
        );
    }

    private BigDecimal computePmt(BigDecimal principal, BigDecimal balloon,
                                   BigDecimal monthlyRate, int n) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.subtract(balloon).divide(BigDecimal.valueOf(n), CALC_SCALE, ROUNDING);
        }

        // (1 + r)^n using BigDecimal.pow — integer exponent, exact
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate).setScale(CALC_SCALE, ROUNDING);
        BigDecimal compoundFactor = onePlusR.pow(n, new MathContext(CALC_SCALE + 5, ROUNDING));

        // PV of balloon = BV / (1+r)^n
        BigDecimal pvBalloon = balloon.divide(compoundFactor, CALC_SCALE, ROUNDING);

        // PMT = (P - PV(balloon)) * [r * (1+r)^n / ((1+r)^n - 1)]
        BigDecimal effectivePrincipal = principal.subtract(pvBalloon);
        BigDecimal numerator = monthlyRate.multiply(compoundFactor).setScale(CALC_SCALE, ROUNDING);
        BigDecimal denominator = compoundFactor.subtract(BigDecimal.ONE).setScale(CALC_SCALE, ROUNDING);

        return effectivePrincipal.multiply(numerator).divide(denominator, CALC_SCALE, ROUNDING);
    }

    private CarLoanResult zeroResult(BigDecimal financedAmount, BigDecimal balloonPayment,
                                      BigDecimal adminFee, int termMonths) {
        BigDecimal zero = BigDecimal.ZERO.setScale(2, ROUNDING);
        return new CarLoanResult(
                financedAmount.max(BigDecimal.ZERO).setScale(2, ROUNDING),
                zero, zero, zero,
                adminFee.multiply(BigDecimal.valueOf(termMonths)).setScale(2, ROUNDING),
                zero, termMonths, true,
                List.of()
        );
    }
}
