package com.psybergate.financialcalculator.exception;

public class CarLoanNotFoundException extends RuntimeException {
    public CarLoanNotFoundException(String message) {
        super(message);
    }
}
