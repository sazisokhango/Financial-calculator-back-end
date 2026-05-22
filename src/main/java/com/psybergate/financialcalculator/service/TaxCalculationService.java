package com.psybergate.financialcalculator.service;

import com.psybergate.financialcalculator.dto.TaxCalculationRequest;
import com.psybergate.financialcalculator.dto.TaxCalculationResponse;
import com.psybergate.financialcalculator.entity.TaxCalculation;
import com.psybergate.financialcalculator.entity.User;
import com.psybergate.financialcalculator.exception.TaxCalculationNotFoundException;
import com.psybergate.financialcalculator.exception.UserNotFoundException;
import com.psybergate.financialcalculator.repository.TaxCalculationRepository;
import com.psybergate.financialcalculator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxCalculationService {

    private final UserRepository userRepository;
    private final TaxCalculationRepository taxCalculationRepository;
    private final SarsTaxCalculator sarsTaxCalculator;

    public TaxCalculationResponse save(TaxCalculationRequest request) {

        User user = userRepository.findByEmailIgnoreCase(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BigDecimal salary            = nullToZero(request.getSalary());
        BigDecimal interestIncome    = nullToZero(request.getInterestIncome());
        BigDecimal dividend          = nullToZero(request.getDividend());
        BigDecimal capitalGain       = nullToZero(request.getCapitalGain());
        BigDecimal bonus             = nullToZero(request.getBonus());
        BigDecimal retirementAnnuity = nullToZero(request.getRetirementAnnuity());
        BigDecimal taxAlreadyPaid    = nullToZero(request.getTaxAlreadyPaid());

        BigDecimal totalIncome      = salary.add(interestIncome).add(dividend).add(capitalGain).add(bonus);
        BigDecimal totalDeductions  = retirementAnnuity;
        BigDecimal netTaxableIncome = totalIncome.subtract(totalDeductions).max(BigDecimal.ZERO)
                                                 .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxBeforeRebate  = sarsTaxCalculator.calculateTax(netTaxableIncome);
        BigDecimal rebate           = sarsTaxCalculator.calculateRebate(request.getAge());
        BigDecimal finalTaxLiability = taxBeforeRebate.subtract(rebate).subtract(taxAlreadyPaid)
                                                      .max(BigDecimal.ZERO)
                                                      .setScale(2, RoundingMode.HALF_UP);

        TaxCalculation entity = TaxCalculation.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .salary(salary)
                .interestIncome(interestIncome)
                .dividend(dividend)
                .capitalGain(capitalGain)
                .bonus(bonus)
                .retirementAnnuity(retirementAnnuity)
                .age(request.getAge())
                .taxAlreadyPaid(taxAlreadyPaid)
                .totalIncome(totalIncome.setScale(2, RoundingMode.HALF_UP))
                .totalDeductions(totalDeductions.setScale(2, RoundingMode.HALF_UP))
                .netTaxableIncome(netTaxableIncome)
                .taxBeforeRebate(taxBeforeRebate)
                .rebate(rebate)
                .finalTaxLiability(finalTaxLiability)
                .build();

        TaxCalculation saved = taxCalculationRepository.save(entity);
        return toResponse(saved);
    }

    public List<TaxCalculationResponse> findAllByUser(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return taxCalculationRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public TaxCalculationResponse findById(Long id) {
        return taxCalculationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new TaxCalculationNotFoundException("Calculation not found"));
    }

    public TaxCalculationResponse update(Long id, TaxCalculationRequest request) {
        TaxCalculation existing = taxCalculationRepository.findById(id)
                .orElseThrow(() -> new TaxCalculationNotFoundException("Calculation not found"));

        userRepository.findByEmailIgnoreCase(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BigDecimal salary            = nullToZero(request.getSalary());
        BigDecimal interestIncome    = nullToZero(request.getInterestIncome());
        BigDecimal dividend          = nullToZero(request.getDividend());
        BigDecimal capitalGain       = nullToZero(request.getCapitalGain());
        BigDecimal bonus             = nullToZero(request.getBonus());
        BigDecimal retirementAnnuity = nullToZero(request.getRetirementAnnuity());
        BigDecimal taxAlreadyPaid    = nullToZero(request.getTaxAlreadyPaid());

        BigDecimal totalIncome      = salary.add(interestIncome).add(dividend).add(capitalGain).add(bonus);
        BigDecimal totalDeductions  = retirementAnnuity;
        BigDecimal netTaxableIncome = totalIncome.subtract(totalDeductions).max(BigDecimal.ZERO)
                                                 .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxBeforeRebate  = sarsTaxCalculator.calculateTax(netTaxableIncome);
        BigDecimal rebate           = sarsTaxCalculator.calculateRebate(request.getAge());
        BigDecimal finalTaxLiability = taxBeforeRebate.subtract(rebate).subtract(taxAlreadyPaid)
                                                      .max(BigDecimal.ZERO)
                                                      .setScale(2, RoundingMode.HALF_UP);

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setSalary(salary);
        existing.setInterestIncome(interestIncome);
        existing.setDividend(dividend);
        existing.setCapitalGain(capitalGain);
        existing.setBonus(bonus);
        existing.setRetirementAnnuity(retirementAnnuity);
        existing.setAge(request.getAge());
        existing.setTaxAlreadyPaid(taxAlreadyPaid);
        existing.setTotalIncome(totalIncome.setScale(2, RoundingMode.HALF_UP));
        existing.setTotalDeductions(totalDeductions.setScale(2, RoundingMode.HALF_UP));
        existing.setNetTaxableIncome(netTaxableIncome);
        existing.setTaxBeforeRebate(taxBeforeRebate);
        existing.setRebate(rebate);
        existing.setFinalTaxLiability(finalTaxLiability);

        return toResponse(taxCalculationRepository.save(existing));
    }

    public void delete(Long id) {
        if (!taxCalculationRepository.existsById(id)) {
            throw new TaxCalculationNotFoundException("Calculation not found");
        }
        taxCalculationRepository.deleteById(id);
    }

    private TaxCalculationResponse toResponse(TaxCalculation tc) {
        return TaxCalculationResponse.builder()
                .id(tc.getId())
                .userId(tc.getUser().getId())
                .title(tc.getTitle())
                .description(tc.getDescription())
                .salary(tc.getSalary())
                .interestIncome(tc.getInterestIncome())
                .dividend(tc.getDividend())
                .capitalGain(tc.getCapitalGain())
                .bonus(tc.getBonus())
                .retirementAnnuity(tc.getRetirementAnnuity())
                .age(tc.getAge())
                .taxAlreadyPaid(tc.getTaxAlreadyPaid())
                .totalIncome(tc.getTotalIncome())
                .totalDeductions(tc.getTotalDeductions())
                .netTaxableIncome(tc.getNetTaxableIncome())
                .taxBeforeRebate(tc.getTaxBeforeRebate())
                .rebate(tc.getRebate())
                .finalTaxLiability(tc.getFinalTaxLiability())
                .build();
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
