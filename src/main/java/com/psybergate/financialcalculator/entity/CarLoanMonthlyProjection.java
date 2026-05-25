package com.psybergate.financialcalculator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "car_loan_monthly_projections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarLoanMonthlyProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_loan_id", nullable = false)
    private CarLoan carLoan;

    @Column(name = "month_number", nullable = false)
    private Integer monthNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal startingBalance;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyRepayment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestCharged;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal adminFee;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal endingBalance;
}
