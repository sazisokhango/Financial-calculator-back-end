package com.psybergate.financialcalculator.controller;

import com.psybergate.financialcalculator.dto.CarLoanRequest;
import com.psybergate.financialcalculator.dto.CarLoanResponse;
import com.psybergate.financialcalculator.service.CarLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class CarLoanController {

    private final CarLoanService carLoanService;

    @PostMapping
    public ResponseEntity<CarLoanResponse> create(@Valid @RequestBody CarLoanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carLoanService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CarLoanResponse>> findAllByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(carLoanService.findAllByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarLoanResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(carLoanService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarLoanResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CarLoanRequest request) {
        return ResponseEntity.ok(carLoanService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carLoanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
