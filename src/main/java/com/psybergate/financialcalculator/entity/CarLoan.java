package com.psybergate.financialcalculator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "car_loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal initialDeposit;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal onceOffFee;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal adminFee;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balloonPayment;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal interestRate;

    // Computed summary fields
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal financedAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyRepayment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRepayments;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterestPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalFeesPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Column(nullable = false)
    private Integer estimatedPayoffMonth;

    @Column(nullable = false)
    private Boolean fullyPaid;

    @OneToMany(mappedBy = "carLoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("monthNumber ASC")
    @Builder.Default
    private List<CarLoanMonthlyProjection> monthlyProjection = new ArrayList<>();
}
