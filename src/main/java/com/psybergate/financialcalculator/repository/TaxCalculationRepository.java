package com.psybergate.financialcalculator.repository;

import com.psybergate.financialcalculator.entity.TaxCalculation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxCalculationRepository extends JpaRepository<TaxCalculation, Long> {
}
