package com.kelvin.coupon_api.exception;

public class InvalidExpirationDateException extends RuntimeException {
    public InvalidExpirationDateException(String message) {
        super(message);
    }
}
