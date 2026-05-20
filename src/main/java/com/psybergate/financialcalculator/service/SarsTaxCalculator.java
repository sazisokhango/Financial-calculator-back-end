package com.psybergate.financialcalculator.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SarsTaxCalculator {

    // SARS 2024/2025 bracket upper thresholds (also used as "over X" subtract values)
    private static final BigDecimal THRESHOLD_2 = new BigDecimal("237100");
    private static final BigDecimal THRESHOLD_3 = new BigDecimal("370500");
    private static final BigDecimal THRESHOLD_4 = new BigDecimal("512800");
    private static final BigDecimal THRESHOLD_5 = new BigDecimal("673000");
    private static final BigDecimal THRESHOLD_6 = new BigDecimal("857900");
    private static final BigDecimal THRESHOLD_7 = new BigDecimal("1817000");

    // Base tax per bracket
    private static final BigDecimal BASE_1 = BigDecimal.ZERO;
    private static final BigDecimal BASE_2 = new BigDecimal("42678");
    private static final BigDecimal BASE_3 = new BigDecimal("77362");
    private static final BigDecimal BASE_4 = new BigDecimal("121475");
    private static final BigDecimal BASE_5 = new BigDecimal("179147");
    private static final BigDecimal BASE_6 = new BigDecimal("251258");
    private static final BigDecimal BASE_7 = new BigDecimal("644489");

    // Marginal rates per bracket
    private static final BigDecimal RATE_1 = new BigDecimal("0.18");
    private static final BigDecimal RATE_2 = new BigDecimal("0.26");
    private static final BigDecimal RATE_3 = new BigDecimal("0.31");
    private static final BigDecimal RATE_4 = new BigDecimal("0.36");
    private static final BigDecimal RATE_5 = new BigDecimal("0.39");
    private static final BigDecimal RATE_6 = new BigDecimal("0.41");
    private static final BigDecimal RATE_7 = new BigDecimal("0.45");

    // SARS 2024/2025 rebates
    private static final BigDecimal REBATE_PRIMARY   = new BigDecimal("17235");
    private static final BigDecimal REBATE_SECONDARY = new BigDecimal("9444");
    private static final BigDecimal REBATE_TERTIARY  = new BigDecimal("3145");

    public BigDecimal calculateTax(BigDecimal taxableIncome) {
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (taxableIncome.compareTo(THRESHOLD_2) <= 0) {
            return apply(BASE_1, RATE_1, taxableIncome, BigDecimal.ZERO);
        } else if (taxableIncome.compareTo(THRESHOLD_3) <= 0) {
            return apply(BASE_2, RATE_2, taxableIncome, THRESHOLD_2);
        } else if (taxableIncome.compareTo(THRESHOLD_4) <= 0) {
            return apply(BASE_3, RATE_3, taxableIncome, THRESHOLD_3);
        } else if (taxableIncome.compareTo(THRESHOLD_5) <= 0) {
            return apply(BASE_4, RATE_4, taxableIncome, THRESHOLD_4);
        } else if (taxableIncome.compareTo(THRESHOLD_6) <= 0) {
            return apply(BASE_5, RATE_5, taxableIncome, THRESHOLD_5);
        } else if (taxableIncome.compareTo(THRESHOLD_7) <= 0) {
            return apply(BASE_6, RATE_6, taxableIncome, THRESHOLD_6);
        } else {
            return apply(BASE_7, RATE_7, taxableIncome, THRESHOLD_7);
        }
    }

    public BigDecimal calculateRebate(int age) {
        BigDecimal rebate = REBATE_PRIMARY;
        if (age >= 65) rebate = rebate.add(REBATE_SECONDARY);
        if (age >= 75) rebate = rebate.add(REBATE_TERTIARY);
        return rebate;
    }

    private BigDecimal apply(BigDecimal base, BigDecimal rate, BigDecimal income, BigDecimal threshold) {
        return base.add(income.subtract(threshold).multiply(rate)).setScale(2, RoundingMode.HALF_UP);
    }
}
