package com.psybergate.financialcalculator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "investment_forecasts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentForecast {

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
    private BigDecimal annualInterestRate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal projectedValue;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalContributions;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterestEarned;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal roiPercentage;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal averageMonthlyGrowth;

    @OneToMany(mappedBy = "forecast", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("month ASC")
    @Builder.Default
    private List<MonthlyProjectionEntry> monthlyProjection = new ArrayList<>();
}
