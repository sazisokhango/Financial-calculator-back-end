package com.psybergate.financialcalculator.repository;

import com.psybergate.financialcalculator.entity.TaxCalculation;
import com.psybergate.financialcalculator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxCalculationRepository extends JpaRepository<TaxCalculation, Long> {

    List<TaxCalculation> findByUser(User user);
}
