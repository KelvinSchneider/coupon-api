package com.kelvin.coupon_api.exception;

public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(String id) {
        super("Cupom não encontrado com o ID: " + id);
    }
}
