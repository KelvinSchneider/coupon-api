package com.kelvin.coupon_api.exception;

public class InvalidDiscountValueException extends RuntimeException {
    public InvalidDiscountValueException(String message) {
        super(message);
    }
}