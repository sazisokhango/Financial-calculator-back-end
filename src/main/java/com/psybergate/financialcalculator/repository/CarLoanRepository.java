package com.psybergate.financialcalculator.repository;

import com.psybergate.financialcalculator.entity.CarLoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarLoanRepository extends JpaRepository<CarLoan, Long> {
    List<CarLoan> findAllByUserEmail(String userEmail);
}
