package com.psybergate.financialcalculator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bond_monthly_projections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BondMonthlyProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_id", nullable = false)
    private PropertyBond bond;

    @Column(name = "month_number", nullable = false)
    private Integer month;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal startingBalance;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestCharged;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal endingBalance;
}
