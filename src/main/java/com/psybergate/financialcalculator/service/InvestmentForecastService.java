package com.psybergate.financialcalculator.service;

import com.psybergate.financialcalculator.dto.*;
import com.psybergate.financialcalculator.entity.InvestmentForecast;
import com.psybergate.financialcalculator.entity.MonthlyProjectionEntry;
import com.psybergate.financialcalculator.entity.User;
import com.psybergate.financialcalculator.exception.InvestmentForecastNotFoundException;
import com.psybergate.financialcalculator.exception.UserNotFoundException;
import com.psybergate.financialcalculator.repository.InvestmentForecastRepository;
import com.psybergate.financialcalculator.repository.UserRepository;
import com.psybergate.financialcalculator.service.InvestmentForecastCalculator.ForecastCalculationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentForecastService {

    private final UserRepository userRepository;
    private final InvestmentForecastRepository forecastRepository;
    private final InvestmentForecastCalculator calculator;

    public InvestmentForecastResponse create(InvestmentForecastRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        ForecastCalculationResult result = calculator.calculate(
                request.getInitialAmount(),
                request.getMonthlyContribution(),
                request.getTermMonths(),
                request.getAnnualInterestRate()
        );

        InvestmentForecast entity = InvestmentForecast.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .initialAmount(request.getInitialAmount())
                .monthlyContribution(request.getMonthlyContribution())
                .termMonths(request.getTermMonths())
                .annualInterestRate(request.getAnnualInterestRate())
                .projectedValue(result.projectedValue())
                .totalContributions(result.totalContributions())
                .totalInterestEarned(result.totalInterestEarned())
                .roiPercentage(result.roiPercentage())
                .averageMonthlyGrowth(result.averageMonthlyGrowth())
                .build();

        List<MonthlyProjectionEntry> entries = result.entries().stream()
                .map(d -> MonthlyProjectionEntry.builder()
                        .forecast(entity)
                        .month(d.getMonth())
                        .startingBalance(d.getStartingBalance())
                        .monthlyContribution(d.getMonthlyContribution())
                        .interestEarned(d.getInterestEarned())
                        .endingBalance(d.getEndingBalance())
                        .build())
                .toList();
        entity.setMonthlyProjection(entries);

        return toResponse(forecastRepository.save(entity));
    }

    public List<InvestmentForecastResponse> findAllByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return forecastRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public InvestmentForecastResponse findById(Long id) {
        return forecastRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new InvestmentForecastNotFoundException("Investment forecast not found"));
    }

    public InvestmentForecastResponse update(Long id, InvestmentForecastRequest request) {
        InvestmentForecast existing = forecastRepository.findById(id)
                .orElseThrow(() -> new InvestmentForecastNotFoundException("Investment forecast not found"));

        if (!existing.getUser().getId().equals(request.getUserId())) {
            throw new IllegalArgumentException("User does not own this forecast");
        }

        ForecastCalculationResult result = calculator.calculate(
                request.getInitialAmount(),
                request.getMonthlyContribution(),
                request.getTermMonths(),
                request.getAnnualInterestRate()
        );

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setInitialAmount(request.getInitialAmount());
        existing.setMonthlyContribution(request.getMonthlyContribution());
        existing.setTermMonths(request.getTermMonths());
        existing.setAnnualInterestRate(request.getAnnualInterestRate());
        existing.setProjectedValue(result.projectedValue());
        existing.setTotalContributions(result.totalContributions());
        existing.setTotalInterestEarned(result.totalInterestEarned());
        existing.setRoiPercentage(result.roiPercentage());
        existing.setAverageMonthlyGrowth(result.averageMonthlyGrowth());

        existing.getMonthlyProjection().clear();
        result.entries().forEach(d -> existing.getMonthlyProjection().add(
                MonthlyProjectionEntry.builder()
                        .forecast(existing)
                        .month(d.getMonth())
                        .startingBalance(d.getStartingBalance())
                        .monthlyContribution(d.getMonthlyContribution())
                        .interestEarned(d.getInterestEarned())
                        .endingBalance(d.getEndingBalance())
                        .build()
        ));

        return toResponse(forecastRepository.save(existing));
    }

    public void delete(Long id) {
        if (!forecastRepository.existsById(id)) {
            throw new InvestmentForecastNotFoundException("Investment forecast not found");
        }
        forecastRepository.deleteById(id);
    }

    private InvestmentForecastResponse toResponse(InvestmentForecast f) {
        ForecastResultDto results = ForecastResultDto.builder()
                .projectedValue(f.getProjectedValue())
                .totalContributions(f.getTotalContributions())
                .totalInterestEarned(f.getTotalInterestEarned())
                .roiPercentage(f.getRoiPercentage())
                .averageMonthlyGrowth(f.getAverageMonthlyGrowth())
                .build();

        List<MonthlyProjectionEntryDto> projection = f.getMonthlyProjection().stream()
                .map(e -> MonthlyProjectionEntryDto.builder()
                        .month(e.getMonth())
                        .startingBalance(e.getStartingBalance())
                        .monthlyContribution(e.getMonthlyContribution())
                        .interestEarned(e.getInterestEarned())
                        .endingBalance(e.getEndingBalance())
                        .build())
                .toList();

        return InvestmentForecastResponse.builder()
                .id(f.getId())
                .userId(f.getUser().getId())
                .title(f.getTitle())
                .description(f.getDescription())
                .initialAmount(f.getInitialAmount())
                .monthlyContribution(f.getMonthlyContribution())
                .termMonths(f.getTermMonths())
                .annualInterestRate(f.getAnnualInterestRate())
                .forecastResults(results)
                .monthlyProjection(projection)
                .build();
    }
}
