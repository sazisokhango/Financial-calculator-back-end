package com.psybergate.financialcalculator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "investment_forecast_monthly_projections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyProjectionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forecast_id", nullable = false)
    private InvestmentForecast forecast;

    @Column(name = "month_number", nullable = false)
    private Integer month;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal startingBalance;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyContribution;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestEarned;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal endingBalance;
}
