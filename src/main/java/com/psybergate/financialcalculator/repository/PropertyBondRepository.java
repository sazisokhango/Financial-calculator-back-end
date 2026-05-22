package com.psybergate.financialcalculator.repository;

import com.psybergate.financialcalculator.entity.PropertyBond;
import com.psybergate.financialcalculator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyBondRepository extends JpaRepository<PropertyBond, Long> {

    List<PropertyBond> findByUser(User user);
}
