package com.psybergate.financialcalculator.controller;

import com.psybergate.financialcalculator.dto.PropertyBondRequest;
import com.psybergate.financialcalculator.dto.PropertyBondResponse;
import com.psybergate.financialcalculator.service.PropertyBondService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bonds")
@RequiredArgsConstructor
public class PropertyBondController {

    private final PropertyBondService bondService;

    @PostMapping
    public ResponseEntity<PropertyBondResponse> create(@Valid @RequestBody PropertyBondRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bondService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<PropertyBondResponse>> getAllByUser(@RequestParam String userEmail) {
        return ResponseEntity.ok(bondService.findAllByUser(userEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyBondResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bondService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyBondResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody PropertyBondRequest request) {
        return ResponseEntity.ok(bondService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bondService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
