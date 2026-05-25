package com.psybergate.financialcalculator.carloan;

import com.psybergate.financialcalculator.dto.CarLoanMonthlyProjectionDto;
import com.psybergate.financialcalculator.service.CarLoanCalculator;
import com.psybergate.financialcalculator.service.CarLoanCalculator.CarLoanResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CarLoanCalculatorTest {

    private final CarLoanCalculator calculator = new CarLoanCalculator();

    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    // ── Standard loan with balloon payment ───────────────────────────────────

    @Test
    void givenStandardLoanWithBalloon_whenCalculate_thenFinancedAmountIsCorrect() {
        CarLoanResult result = calculator.calculate(
                bd("500000"), bd("80000"), bd("1500"),
                bd("69"), bd("0"), 60, bd("12")
        );
        assertThat(result.financedAmount()).isEqualByComparingTo(bd("421500.00"));
    }

    @Test
    void givenStandardLoanWithBalloon_whenCalculate_thenMonthlyRepaymentIncludesAdminFee() {
        CarLoanResult result = calculator.calculate(
                bd("300000"), bd("50000"), bd("0"),
                bd("69"), bd("50000"), 60, bd("12")
        );
        // monthlyRepayment must be PMT + 69
        BigDecimal pmt = result.monthlyRepayment().subtract(bd("69"));
        assertThat(pmt).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.monthlyRepayment()).isGreaterThan(bd("69"));
    }

    @Test
    void givenStandardLoan_whenCalculate_thenMonth1ProjectionIsCorrect() {
        // 250000 financed, 12% pa, 60 months, no balloon, adminFee=100
        CarLoanResult result = calculator.calculate(
                bd("250000"), bd("0"), bd("0"),
                bd("100"), bd("0"), 60, bd("12")
        );

        CarLoanMonthlyProjectionDto month1 = result.monthlyProjection().get(0);
        BigDecimal expectedInterest = bd("250000").multiply(bd("0.01")).setScale(2, java.math.RoundingMode.HALF_UP);

        assertThat(month1.getMonth()).isEqualTo(1);
        assertThat(month1.getStartingBalance()).isEqualByComparingTo(bd("250000.00"));
        assertThat(month1.getInterestCharged()).isEqualByComparingTo(expectedInterest);
        assertThat(month1.getAdminFee()).isEqualByComparingTo(bd("100.00"));
        // principalPaid = PMT - interestCharged
        BigDecimal pmt = result.monthlyRepayment().subtract(bd("100"));
        assertThat(month1.getPrincipalPaid()).isEqualByComparingTo(pmt.subtract(expectedInterest));
        // endingBalance = startingBalance - principalPaid
        assertThat(month1.getEndingBalance()).isEqualByComparingTo(
                bd("250000.00").subtract(month1.getPrincipalPaid()));
    }

    @Test
    void givenStandardLoan_whenCalculate_thenFinalMonthEndingBalanceIsZero() {
        CarLoanResult result = calculator.calculate(
                bd("120000"), bd("0"), bd("0"),
                bd("0"), bd("0"), 24, bd("10")
        );

        CarLoanMonthlyProjectionDto lastMonth = result.monthlyProjection().get(23);
        assertThat(lastMonth.getEndingBalance()).isEqualByComparingTo(bd("0.00"));
        assertThat(result.remainingBalance()).isEqualByComparingTo(bd("0.00"));
        assertThat(result.fullyPaid()).isTrue();
    }

    @Test
    void givenBalloonPayment_whenCalculate_thenFinalMonthAppliesBalloon() {
        CarLoanResult result = calculator.calculate(
                bd("200000"), bd("0"), bd("0"),
                bd("0"), bd("50000"), 36, bd("12")
        );

        CarLoanMonthlyProjectionDto lastMonth = result.monthlyProjection().get(35);
        assertThat(lastMonth.getEndingBalance()).isEqualByComparingTo(bd("0.00"));
        assertThat(result.fullyPaid()).isTrue();
        assertThat(result.remainingBalance()).isEqualByComparingTo(bd("0.00"));
    }

    @Test
    void givenStandardLoan_whenCalculate_thenTotalRepaymentsMatchesSumOfProjection() {
        CarLoanResult result = calculator.calculate(
                bd("100000"), bd("0"), bd("0"),
                bd("50"), bd("0"), 12, bd("10")
        );

        BigDecimal sumFromProjection = result.monthlyProjection().stream()
                .map(CarLoanMonthlyProjectionDto::getMonthlyRepayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        assertThat(result.totalRepayments()).isEqualByComparingTo(sumFromProjection);
    }

    @Test
    void givenStandardLoan_whenCalculate_thenTotalFeesPaidIsAdminFeeTimesTerms() {
        CarLoanResult result = calculator.calculate(
                bd("100000"), bd("0"), bd("0"),
                bd("69"), bd("0"), 12, bd("10")
        );

        assertThat(result.totalFeesPaid()).isEqualByComparingTo(bd("69").multiply(BigDecimal.valueOf(12)));
    }

    // ── Zero interest rate ────────────────────────────────────────────────────

    @Test
    void givenZeroInterestRate_whenCalculate_thenInstalmentIsPrincipalDividedByTerm() {
        CarLoanResult result = calculator.calculate(
                bd("120000"), bd("0"), bd("0"),
                bd("0"), bd("0"), 12, bd("0")
        );

        assertThat(result.monthlyRepayment()).isEqualByComparingTo(bd("10000.00"));
        assertThat(result.totalInterestPaid()).isEqualByComparingTo(bd("0.00"));
    }

    @Test
    void givenZeroInterestRateWithBalloon_whenCalculate_thenInstalmentIsAdjusted() {
        // PMT = (120000 - 20000) / 12 = 8333.33
        CarLoanResult result = calculator.calculate(
                bd("120000"), bd("0"), bd("0"),
                bd("0"), bd("20000"), 12, bd("0")
        );

        BigDecimal expectedPmt = bd("100000").divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
        assertThat(result.monthlyRepayment()).isEqualByComparingTo(expectedPmt);
        assertThat(result.totalInterestPaid()).isEqualByComparingTo(bd("0.00"));
    }

    // ── Zero financed amount ──────────────────────────────────────────────────

    @Test
    void givenDepositEqualsPurchasePrice_whenCalculate_thenReturnsEmptySchedule() {
        CarLoanResult result = calculator.calculate(
                bd("100000"), bd("100000"), bd("0"),
                bd("69"), bd("0"), 12, bd("10")
        );

        assertThat(result.financedAmount()).isEqualByComparingTo(bd("0.00"));
        assertThat(result.monthlyProjection()).isEmpty();
    }

    // ── Repayment too low (negative PMT — balloon exceeds principal) ──────────

    @Test
    void givenBalloonExceedsFinancedAmount_whenCalculatorCalledDirectly_thenThrowsIllegalArgument() {
        // Calculator called directly (bypassing service validation):
        // balloon (200000) > financedAmount (100000) → negative PMT
        assertThatThrownBy(() -> calculator.calculate(
                bd("100000"), bd("0"), bd("0"),
                bd("0"), bd("200000"), 12, bd("12")
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Monthly repayment amount is too low to reduce the loan balance.");
    }

    // ── Schedule continuity ───────────────────────────────────────────────────

    @Test
    void givenValidLoan_whenCalculate_thenEachMonthStartsWhereLastEnded() {
        CarLoanResult result = calculator.calculate(
                bd("50000"), bd("0"), bd("0"),
                bd("0"), bd("0"), 6, bd("10")
        );

        List<CarLoanMonthlyProjectionDto> schedule = result.monthlyProjection();
        for (int i = 1; i < schedule.size(); i++) {
            assertThat(schedule.get(i).getStartingBalance())
                    .isEqualByComparingTo(schedule.get(i - 1).getEndingBalance());
        }
    }
}
