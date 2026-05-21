package com.psybergate.financialcalculator.controller;

import com.psybergate.financialcalculator.dto.InvestmentForecastRequest;
import com.psybergate.financialcalculator.dto.InvestmentForecastResponse;
import com.psybergate.financialcalculator.service.InvestmentForecastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentForecastController {

    private final InvestmentForecastService forecastService;

    @PostMapping("/forecast")
    public ResponseEntity<InvestmentForecastResponse> create(@Valid @RequestBody InvestmentForecastRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(forecastService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<InvestmentForecastResponse>> getAllByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(forecastService.findAllByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentForecastResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(forecastService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentForecastResponse> update(@PathVariable Long id,
                                                              @Valid @RequestBody InvestmentForecastRequest request) {
        return ResponseEntity.ok(forecastService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        forecastService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
