package com.kelvin.coupon_api.domain;

import com.kelvin.coupon_api.exception.CouponAlreadyDeletedException;
import com.kelvin.coupon_api.exception.InvalidCouponCodeException;
import com.kelvin.coupon_api.exception.InvalidDiscountValueException;
import com.kelvin.coupon_api.exception.InvalidExpirationDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CouponTest {

    private static final OffsetDateTime FUTURE = OffsetDateTime.now().plusDays(30);

    // CREATE

    @Nested
    @DisplayName("Coupon.create()")
    class Create {

        @Test
        @DisplayName("should create coupon with valid data")
        void shouldCreateWithValidData() {
            Coupon coupon = Coupon.create("ABC123", "desc", new BigDecimal("1.0"), FUTURE, false);

            assertThat(coupon.getCode()).isEqualTo("ABC123");
            assertThat(coupon.getDescription()).isEqualTo("desc");
            assertThat(coupon.getDiscountValue()).isEqualByComparingTo("1.0");
            assertThat(coupon.getExpirationDate()).isEqualTo(FUTURE);
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ACTIVE);
            assertThat(coupon.isRedeemed()).isFalse();
            assertThat(coupon.isDeleted()).isFalse();
            assertThat(coupon.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should remove special characters from code")
        void shouldSanitizeSpecialCharacters() {
            Coupon coupon = Coupon.create("ABC-123", "desc", new BigDecimal("1.0"), FUTURE, false);
            assertThat(coupon.getCode()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("should uppercase the code")
        void shouldUppercaseCode() {
            Coupon coupon = Coupon.create("abc123", "desc", new BigDecimal("1.0"), FUTURE, false);
            assertThat(coupon.getCode()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("should trim code to 6 chars after sanitization")
        void shouldTrimCodeToSixChars() {
            Coupon coupon = Coupon.create("ABC-123-XYZ", "desc", new BigDecimal("1.0"), FUTURE, false);
            assertThat(coupon.getCode()).hasSize(6);
            assertThat(coupon.getCode()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("should fail when sanitized code is shorter than 6 characters")
        void shouldFailWhenCodeTooShort() {
            assertThatThrownBy(() -> Coupon.create("AB", "desc", new BigDecimal("1.0"), FUTURE, false))
                    .isInstanceOf(InvalidCouponCodeException.class);
        }

        @Test
        @DisplayName("should fail when code is null")
        void shouldFailWhenCodeNull() {
            assertThatThrownBy(() -> Coupon.create(null, "desc", new BigDecimal("1.0"), FUTURE, false))
                    .isInstanceOf(InvalidCouponCodeException.class);
        }

        @Test
        @DisplayName("should fail when discount value is below minimum (0.5)")
        void shouldFailWhenDiscountBelowMinimum() {
            assertThatThrownBy(() -> Coupon.create("ABC123", "desc", new BigDecimal("0.4"), FUTURE, false))
                    .isInstanceOf(InvalidDiscountValueException.class)
                    .hasMessageContaining("0.5");
        }

        @Test
        @DisplayName("should accept discount value exactly at minimum (0.5)")
        void shouldAcceptMinimumDiscount() {
            assertThatNoException().isThrownBy(
                    () -> Coupon.create("ABC123", "desc", new BigDecimal("0.5"), FUTURE, false));
        }

        @Test
        @DisplayName("should accept any discount value above minimum")
        void shouldAcceptLargeDiscount() {
            assertThatNoException().isThrownBy(
                    () -> Coupon.create("ABC123", "desc", new BigDecimal("9999.99"), FUTURE, false));
        }

        @Test
        @DisplayName("should fail when discount value is null")
        void shouldFailWhenDiscountNull() {
            assertThatThrownBy(() -> Coupon.create("ABC123", "desc", null, FUTURE, false))
                    .isInstanceOf(InvalidDiscountValueException.class);
        }

        @Test
        @DisplayName("should fail when expiration date is in the past")
        void shouldFailWhenExpirationInPast() {
            OffsetDateTime past = OffsetDateTime.now().minusDays(1);
            assertThatThrownBy(() -> Coupon.create("ABC123", "desc", new BigDecimal("1.0"), past, false))
                    .isInstanceOf(InvalidExpirationDateException.class);
        }

        @Test
        @DisplayName("should fail when expiration date is null")
        void shouldFailWhenExpirationNull() {
            assertThatThrownBy(() -> Coupon.create("ABC123", "desc", new BigDecimal("1.0"), null, false))
                    .isInstanceOf(InvalidExpirationDateException.class);
        }

        @Test
        @DisplayName("should allow creation as already published")
        void shouldAllowCreationAsPublished() {
            Coupon coupon = Coupon.create("ABC123", "desc", new BigDecimal("1.0"), FUTURE, true);
            assertThat(coupon.isPublished()).isTrue();
        }

        @Test
        @DisplayName("should allow creation as unpublished")
        void shouldAllowCreationAsUnpublished() {
            Coupon coupon = Coupon.create("ABC123", "desc", new BigDecimal("1.0"), FUTURE, false);
            assertThat(coupon.isPublished()).isFalse();
        }
    }

    // SOFT DELETE

    @Nested
    @DisplayName("Coupon.softDelete()")
    class SoftDelete {

        @Test
        @DisplayName("should mark coupon as deleted and set deletedAt")
        void shouldMarkAsDeleted() {
            Coupon coupon = Coupon.create("ABC123", "desc", new BigDecimal("1.0"), FUTURE, false);
            coupon.softDelete();

            assertThat(coupon.isDeleted()).isTrue();
            assertThat(coupon.getDeletedAt()).isNotNull();
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.DELETED);
        }

        @Test
        @DisplayName("should fail when deleting an already deleted coupon")
        void shouldFailWhenAlreadyDeleted() {
            Coupon coupon = Coupon.create("ABC123", "desc", new BigDecimal("1.0"), FUTURE, false);
            coupon.softDelete();

            assertThatThrownBy(coupon::softDelete)
                    .isInstanceOf(CouponAlreadyDeletedException.class);
        }

        @Test
        @DisplayName("should not physically remove the coupon from the database (data preserved)")
        void shouldPreserveData() {
            Coupon coupon = Coupon.create("ABC123", "Some description", new BigDecimal("2.5"), FUTURE, true);
            coupon.softDelete();

            assertThat(coupon.getCode()).isEqualTo("ABC123");
            assertThat(coupon.getDescription()).isEqualTo("Some description");
            assertThat(coupon.getDiscountValue()).isEqualByComparingTo("2.5");
        }
    }

}
