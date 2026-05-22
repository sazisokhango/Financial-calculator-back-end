package com.psybergate.financialcalculator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "property_bonds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyBond {

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
    private BigDecimal initialAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyContribution;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal interestRate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalLoanAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRepayments;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterestPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Column(nullable = false)
    private Integer estimatedPayoffMonth;

    @Column(nullable = false)
    private Boolean fullyPaid;

    @OneToMany(mappedBy = "bond", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("month_number ASC")
    @Builder.Default
    private List<BondMonthlyProjection> monthlyProjection = new ArrayList<>();
}
