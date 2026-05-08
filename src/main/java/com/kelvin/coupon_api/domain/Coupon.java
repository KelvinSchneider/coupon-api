package com.kelvin.coupon_api.domain;

import com.kelvin.coupon_api.exception.CouponAlreadyDeletedException;
import com.kelvin.coupon_api.exception.InvalidCouponCodeException;
import com.kelvin.coupon_api.exception.InvalidDiscountValueException;
import com.kelvin.coupon_api.exception.InvalidExpirationDateException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class Coupon {

    private static final int CODE_LENGTH = 6;
    private static final BigDecimal MIN_DISCOUNT_VALUE = new BigDecimal("0.5");

    private UUID id;
    private String code;
    private String description;
    private BigDecimal discountValue;
    private OffsetDateTime expirationDate;
    private CouponStatus status;
    private boolean published;
    private boolean redeemed;
    private boolean deleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime deletedAt;

    private Coupon() {}

    public static Coupon create(String code,
                                String description,
                                BigDecimal discountValue,
                                OffsetDateTime expirationDate,
                                boolean published) {
        String sanitizedCode = sanitizeCode(code);
        validateCode(sanitizedCode);
        validateDiscountValue(discountValue);
        validateExpirationDate(expirationDate);

        Coupon coupon = new Coupon();
        coupon.code = sanitizedCode;
        coupon.description = description;
        coupon.discountValue = discountValue;
        coupon.expirationDate = expirationDate;
        coupon.published = published;
        coupon.status = CouponStatus.ACTIVE;
        coupon.redeemed = false;
        coupon.deleted = false;
        coupon.createdAt = OffsetDateTime.now();
        return coupon;
    }

    public static Coupon reconstitute(UUID id, String code, String description,
                                      BigDecimal discountValue, OffsetDateTime expirationDate,
                                      CouponStatus status, boolean published,
                                      boolean redeemed, boolean deleted,
                                      OffsetDateTime createdAt, OffsetDateTime deletedAt) {
        Coupon coupon = new Coupon();
        coupon.id = id;
        coupon.code = code;
        coupon.description = description;
        coupon.discountValue = discountValue;
        coupon.expirationDate = expirationDate;
        coupon.status = status;
        coupon.published = published;
        coupon.redeemed = redeemed;
        coupon.deleted = deleted;
        coupon.createdAt = createdAt;
        coupon.deletedAt = deletedAt;
        return coupon;
    }

    public void softDelete() {
        if (this.deleted) {
            throw new CouponAlreadyDeletedException();
        }
        this.deleted = true;
        this.deletedAt = OffsetDateTime.now();
        this.status = CouponStatus.DELETED;
    }

    private static String sanitizeCode(String code) {
        if (code == null) return "";
        String onlyAlphanumeric = code.replaceAll("[^A-Za-z0-9]", "");
        if (onlyAlphanumeric.length() > CODE_LENGTH) {
            return onlyAlphanumeric.substring(0, CODE_LENGTH).toUpperCase();
        }
        return onlyAlphanumeric.toUpperCase();
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidCouponCodeException("O código não pode estar em branco.");
        }
        if (code.length() != CODE_LENGTH) {
            throw new InvalidCouponCodeException (
                    "O código deve ter exatamente " + CODE_LENGTH + " Caracteres alfanuméricos após a sanitização. Obtido: '" + code + "'");
        }
    }

    private static void validateDiscountValue(BigDecimal value) {
        if (value == null) {
            throw new InvalidDiscountValueException("O valor do desconto não pode ser nulo.");
        }
        if (value.compareTo(MIN_DISCOUNT_VALUE) < 0) {
            throw new InvalidDiscountValueException(
                    "O valor de desconto deve ser no mínimo " + MIN_DISCOUNT_VALUE + ".");
        }
    }

    private static void validateExpirationDate(OffsetDateTime date) {
        if (date == null) {
            throw new InvalidExpirationDateException("A data de validade não pode ser nula.");
        }
        if (date.isBefore(OffsetDateTime.now())) {
            throw new InvalidExpirationDateException("A data de expiração não pode estar no passado. Recebida: " + date);
        }
    }
}
