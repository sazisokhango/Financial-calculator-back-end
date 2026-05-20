package com.psybergate.financialcalculator.controller;

import com.psybergate.financialcalculator.dto.TaxCalculationRequest;
import com.psybergate.financialcalculator.dto.TaxCalculationResponse;
import com.psybergate.financialcalculator.service.TaxCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxCalculationService taxCalculationService;

    @PostMapping
    public ResponseEntity<TaxCalculationResponse> calculate(@Valid @RequestBody TaxCalculationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taxCalculationService.save(request));
    }
}
