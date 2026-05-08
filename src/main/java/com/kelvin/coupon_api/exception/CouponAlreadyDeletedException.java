package com.kelvin.coupon_api.exception;

public class CouponAlreadyDeletedException extends RuntimeException {
    public CouponAlreadyDeletedException() {
        super("O cupom já foi deletado.");
    }
}
