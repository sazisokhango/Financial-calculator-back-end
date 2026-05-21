package com.psybergate.financialcalculator.repository;

import com.psybergate.financialcalculator.entity.InvestmentForecast;
import com.psybergate.financialcalculator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentForecastRepository extends JpaRepository<InvestmentForecast, Long> {
    List<InvestmentForecast> findByUser(User user);
}
