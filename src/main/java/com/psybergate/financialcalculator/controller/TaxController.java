package com.psybergate.financialcalculator.controller;

import com.psybergate.financialcalculator.dto.TaxCalculationRequest;
import com.psybergate.financialcalculator.dto.TaxCalculationResponse;
import com.psybergate.financialcalculator.service.TaxCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxCalculationService taxCalculationService;

    @PostMapping
    public ResponseEntity<TaxCalculationResponse> calculate(@Valid @RequestBody TaxCalculationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taxCalculationService.save(request));
    }

    @GetMapping
    public ResponseEntity<List<TaxCalculationResponse>> getAllByUser(@RequestParam String userEmail) {
        return ResponseEntity.ok(taxCalculationService.findAllByUser(userEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaxCalculationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taxCalculationService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxCalculationResponse> update(@PathVariable Long id,
                                                         @Valid @RequestBody TaxCalculationRequest request) {
        return ResponseEntity.ok(taxCalculationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taxCalculationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
