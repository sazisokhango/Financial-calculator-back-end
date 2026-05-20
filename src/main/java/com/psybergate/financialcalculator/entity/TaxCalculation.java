package com.psybergate.financialcalculator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tax_calculations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salary;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestIncome;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal dividend;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal capitalGain;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bonus;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal retirementAnnuity;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAlreadyPaid;

    // Computed breakdown fields
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalIncome;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductions;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netTaxableIncome;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal taxBeforeRebate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal rebate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal finalTaxLiability;
}
