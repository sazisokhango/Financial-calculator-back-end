package com.psybergate.financialcalculator.service;

import com.psybergate.financialcalculator.dto.*;
import com.psybergate.financialcalculator.entity.BondMonthlyProjection;
import com.psybergate.financialcalculator.entity.PropertyBond;
import com.psybergate.financialcalculator.entity.User;
import com.psybergate.financialcalculator.exception.PropertyBondNotFoundException;
import com.psybergate.financialcalculator.exception.UserNotFoundException;
import com.psybergate.financialcalculator.repository.PropertyBondRepository;
import com.psybergate.financialcalculator.repository.UserRepository;
import com.psybergate.financialcalculator.service.PropertyBondCalculator.BondCalculationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyBondService {

    private final UserRepository userRepository;
    private final PropertyBondRepository bondRepository;
    private final PropertyBondCalculator calculator;

    public PropertyBondResponse create(PropertyBondRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getUserEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BondCalculationResult result = calculator.calculate(
                request.getInitialAmount(),
                request.getMonthlyContribution(),
                request.getTermMonths(),
                request.getInterestRate()
        );

        PropertyBond entity = PropertyBond.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .initialAmount(request.getInitialAmount())
                .monthlyContribution(request.getMonthlyContribution())
                .termMonths(request.getTermMonths())
                .interestRate(request.getInterestRate())
                .totalLoanAmount(result.totalLoanAmount())
                .totalRepayments(result.totalRepayments())
                .totalInterestPaid(result.totalInterestPaid())
                .remainingBalance(result.remainingBalance())
                .estimatedPayoffMonth(result.estimatedPayoffMonth())
                .fullyPaid(result.fullyPaid())
                .build();

        List<BondMonthlyProjection> projections = result.entries().stream()
                .map(d -> BondMonthlyProjection.builder()
                        .bond(entity)
                        .month(d.getMonth())
                        .startingBalance(d.getStartingBalance())
                        .monthlyPayment(d.getMonthlyPayment())
                        .interestCharged(d.getInterestCharged())
                        .principalPaid(d.getPrincipalPaid())
                        .endingBalance(d.getEndingBalance())
                        .build())
                .toList();
        entity.setMonthlyProjection(projections);

        return toResponse(bondRepository.save(entity));
    }

    public List<PropertyBondResponse> findAllByUser(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return bondRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public PropertyBondResponse findById(Long id) {
        return bondRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new PropertyBondNotFoundException("Property bond not found"));
    }

    public PropertyBondResponse update(Long id, PropertyBondRequest request) {
        PropertyBond existing = bondRepository.findById(id)
                .orElseThrow(() -> new PropertyBondNotFoundException("Property bond not found"));

        BondCalculationResult result = calculator.calculate(
                request.getInitialAmount(),
                request.getMonthlyContribution(),
                request.getTermMonths(),
                request.getInterestRate()
        );

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setInitialAmount(request.getInitialAmount());
        existing.setMonthlyContribution(request.getMonthlyContribution());
        existing.setTermMonths(request.getTermMonths());
        existing.setInterestRate(request.getInterestRate());
        existing.setTotalLoanAmount(result.totalLoanAmount());
        existing.setTotalRepayments(result.totalRepayments());
        existing.setTotalInterestPaid(result.totalInterestPaid());
        existing.setRemainingBalance(result.remainingBalance());
        existing.setEstimatedPayoffMonth(result.estimatedPayoffMonth());
        existing.setFullyPaid(result.fullyPaid());

        existing.getMonthlyProjection().clear();
        result.entries().forEach(d -> existing.getMonthlyProjection().add(
                BondMonthlyProjection.builder()
                        .bond(existing)
                        .month(d.getMonth())
                        .startingBalance(d.getStartingBalance())
                        .monthlyPayment(d.getMonthlyPayment())
                        .interestCharged(d.getInterestCharged())
                        .principalPaid(d.getPrincipalPaid())
                        .endingBalance(d.getEndingBalance())
                        .build()
        ));

        return toResponse(bondRepository.save(existing));
    }

    public void delete(Long id) {
        if (!bondRepository.existsById(id)) {
            throw new PropertyBondNotFoundException("Property bond not found");
        }
        bondRepository.deleteById(id);
    }

    private PropertyBondResponse toResponse(PropertyBond b) {
        BondForecastResultDto results = BondForecastResultDto.builder()
                .totalLoanAmount(b.getTotalLoanAmount())
                .totalRepayments(b.getTotalRepayments())
                .totalInterestPaid(b.getTotalInterestPaid())
                .remainingBalance(b.getRemainingBalance())
                .estimatedPayoffMonth(b.getEstimatedPayoffMonth())
                .fullyPaid(b.getFullyPaid())
                .build();

        List<BondMonthlyProjectionDto> projection = b.getMonthlyProjection().stream()
                .map(e -> BondMonthlyProjectionDto.builder()
                        .month(e.getMonth())
                        .startingBalance(e.getStartingBalance())
                        .monthlyPayment(e.getMonthlyPayment())
                        .interestCharged(e.getInterestCharged())
                        .principalPaid(e.getPrincipalPaid())
                        .endingBalance(e.getEndingBalance())
                        .build())
                .toList();

        return PropertyBondResponse.builder()
                .id(b.getId())
                .userEmail(b.getUser().getEmail())
                .title(b.getTitle())
                .description(b.getDescription())
                .initialAmount(b.getInitialAmount())
                .monthlyContribution(b.getMonthlyContribution())
                .termMonths(b.getTermMonths())
                .interestRate(b.getInterestRate())
                .forecastResults(results)
                .monthlyProjection(projection)
                .build();
    }
}
