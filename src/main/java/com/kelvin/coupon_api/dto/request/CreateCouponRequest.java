package com.kelvin.coupon_api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateCouponRequest(
        @NotBlank(message = "code is required")
        String code,

        @NotBlank(message = "description is required")
        String description,

        @NotNull(message = "discountValue is required")
        BigDecimal discountValue,

        @NotNull(message = "expirationDate is required")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        OffsetDateTime expirationDate,

        boolean published
) {}
