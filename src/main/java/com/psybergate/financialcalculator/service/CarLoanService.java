package com.psybergate.financialcalculator.service;

import com.psybergate.financialcalculator.dto.*;
import com.psybergate.financialcalculator.entity.CarLoan;
import com.psybergate.financialcalculator.entity.CarLoanMonthlyProjection;
import com.psybergate.financialcalculator.entity.User;
import com.psybergate.financialcalculator.exception.CarLoanNotFoundException;
import com.psybergate.financialcalculator.exception.UserNotFoundException;
import com.psybergate.financialcalculator.repository.CarLoanRepository;
import com.psybergate.financialcalculator.repository.UserRepository;
import com.psybergate.financialcalculator.service.CarLoanCalculator.CarLoanResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarLoanService {

    private final CarLoanRepository carLoanRepository;
    private final CarLoanCalculator calculator;
    private final UserRepository userRepository;

    public CarLoanResponse create(CarLoanRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));
        validate(request);
        CarLoanResult result = calculator.calculate(
                request.getPurchasePrice(), request.getInitialDeposit(),
                request.getOnceOffFee(), request.getAdminFee(),
                request.getBalloonPayment(), request.getTermMonths(),
                request.getInterestRate()
        );
        CarLoan entity = buildEntity(request, result, user);
        return toResponse(carLoanRepository.save(entity));
    }

    public List<CarLoanResponse> findAllByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return carLoanRepository.findAllByUser_Id(userId).stream().map(this::toResponse).toList();
    }

    public CarLoanResponse findById(Long id) {
        return carLoanRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new CarLoanNotFoundException("Car loan not found with id: " + id));
    }

    public CarLoanResponse update(Long id, CarLoanRequest request) {
        CarLoan existing = carLoanRepository.findById(id)
                .orElseThrow(() -> new CarLoanNotFoundException("Car loan not found with id: " + id));
        validate(request);
        CarLoanResult result = calculator.calculate(
                request.getPurchasePrice(), request.getInitialDeposit(),
                request.getOnceOffFee(), request.getAdminFee(),
                request.getBalloonPayment(), request.getTermMonths(),
                request.getInterestRate()
        );

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setPurchasePrice(request.getPurchasePrice());
        existing.setInitialDeposit(request.getInitialDeposit());
        existing.setOnceOffFee(request.getOnceOffFee());
        existing.setAdminFee(request.getAdminFee());
        existing.setBalloonPayment(request.getBalloonPayment());
        existing.setTermMonths(request.getTermMonths());
        existing.setInterestRate(request.getInterestRate());
        applySummary(existing, result);

        existing.getMonthlyProjection().clear();
        result.monthlyProjection().forEach(d -> existing.getMonthlyProjection().add(toProjectionEntity(d, existing)));

        return toResponse(carLoanRepository.save(existing));
    }

    public void delete(Long id) {
        if (!carLoanRepository.existsById(id)) {
            throw new CarLoanNotFoundException("Car loan not found with id: " + id);
        }
        carLoanRepository.deleteById(id);
    }

    private void validate(CarLoanRequest request) {
        if (request.getInitialDeposit().compareTo(request.getPurchasePrice()) > 0) {
            throw new IllegalArgumentException("Initial deposit cannot exceed purchase price.");
        }
        BigDecimal financedAmount = request.getPurchasePrice()
                .subtract(request.getInitialDeposit())
                .add(request.getOnceOffFee());
        if (request.getBalloonPayment().compareTo(financedAmount) > 0) {
            throw new IllegalArgumentException("Balloon payment cannot exceed the financed amount.");
        }
    }

    private CarLoan buildEntity(CarLoanRequest request, CarLoanResult result, User user) {
        CarLoan entity = CarLoan.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .purchasePrice(request.getPurchasePrice())
                .initialDeposit(request.getInitialDeposit())
                .onceOffFee(request.getOnceOffFee())
                .adminFee(request.getAdminFee())
                .balloonPayment(request.getBalloonPayment())
                .termMonths(request.getTermMonths())
                .interestRate(request.getInterestRate())
                .build();
        applySummary(entity, result);
        List<CarLoanMonthlyProjection> projections = result.monthlyProjection().stream()
                .map(d -> toProjectionEntity(d, entity))
                .toList();
        entity.setMonthlyProjection(projections);
        return entity;
    }

    private void applySummary(CarLoan entity, CarLoanResult result) {
        entity.setFinancedAmount(result.financedAmount());
        entity.setMonthlyRepayment(result.monthlyRepayment());
        entity.setTotalRepayments(result.totalRepayments());
        entity.setTotalInterestPaid(result.totalInterestPaid());
        entity.setTotalFeesPaid(result.totalFeesPaid());
        entity.setRemainingBalance(result.remainingBalance());
        entity.setEstimatedPayoffMonth(result.estimatedPayoffMonth());
        entity.setFullyPaid(result.fullyPaid());
    }

    private CarLoanMonthlyProjection toProjectionEntity(CarLoanMonthlyProjectionDto d, CarLoan loan) {
        return CarLoanMonthlyProjection.builder()
                .carLoan(loan)
                .monthNumber(d.getMonth())
                .startingBalance(d.getStartingBalance())
                .monthlyRepayment(d.getMonthlyRepayment())
                .interestCharged(d.getInterestCharged())
                .adminFee(d.getAdminFee())
                .principalPaid(d.getPrincipalPaid())
                .endingBalance(d.getEndingBalance())
                .build();
    }

    private CarLoanResponse toResponse(CarLoan loan) {
        CarLoanForecastResultDto forecast = CarLoanForecastResultDto.builder()
                .financedAmount(loan.getFinancedAmount())
                .monthlyRepayment(loan.getMonthlyRepayment())
                .totalRepayments(loan.getTotalRepayments())
                .totalInterestPaid(loan.getTotalInterestPaid())
                .totalFeesPaid(loan.getTotalFeesPaid())
                .balloonPayment(loan.getBalloonPayment())
                .remainingBalance(loan.getRemainingBalance())
                .estimatedPayoffMonth(loan.getEstimatedPayoffMonth())
                .fullyPaid(loan.getFullyPaid())
                .build();

        List<CarLoanMonthlyProjectionDto> projection = loan.getMonthlyProjection().stream()
                .map(e -> CarLoanMonthlyProjectionDto.builder()
                        .month(e.getMonthNumber())
                        .startingBalance(e.getStartingBalance())
                        .monthlyRepayment(e.getMonthlyRepayment())
                        .interestCharged(e.getInterestCharged())
                        .adminFee(e.getAdminFee())
                        .principalPaid(e.getPrincipalPaid())
                        .endingBalance(e.getEndingBalance())
                        .build())
                .toList();

        return CarLoanResponse.builder()
                .id(loan.getId())
                .title(loan.getTitle())
                .description(loan.getDescription())
                .purchasePrice(loan.getPurchasePrice())
                .initialDeposit(loan.getInitialDeposit())
                .onceOffFee(loan.getOnceOffFee())
                .adminFee(loan.getAdminFee())
                .balloonPayment(loan.getBalloonPayment())
                .termMonths(loan.getTermMonths())
                .interestRate(loan.getInterestRate())
                .forecastResults(forecast)
                .monthlyProjection(projection)
                .build();
    }
}
