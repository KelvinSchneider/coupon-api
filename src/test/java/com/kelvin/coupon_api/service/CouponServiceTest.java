package com.kelvin.coupon_api.service;

import com.kelvin.coupon_api.domain.Coupon;
import com.kelvin.coupon_api.domain.CouponStatus;
import com.kelvin.coupon_api.dto.request.CreateCouponRequest;
import com.kelvin.coupon_api.dto.response.CouponResponse;
import com.kelvin.coupon_api.exception.CouponAlreadyDeletedException;
import com.kelvin.coupon_api.exception.CouponNotFoundException;
import com.kelvin.coupon_api.mapper.CouponMapper;
import com.kelvin.coupon_api.persistence.CouponEntity;
import com.kelvin.coupon_api.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository repository;

    @Mock
    private CouponMapper mapper;

    @InjectMocks
    private CouponService service;

    private static final OffsetDateTime FUTURE = OffsetDateTime.now().plusDays(30);

    private Coupon buildDomain() {
        return Coupon.create("ABC123", "desc", new BigDecimal("1.0"), FUTURE, false);
    }

    private CouponEntity buildEntity() {
        CouponEntity entity = new CouponEntity();
        entity.setCode("ABC123");
        entity.setDescription("desc");
        entity.setDiscountValue(new BigDecimal("1.0"));
        entity.setExpirationDate(FUTURE);
        entity.setStatus(CouponStatus.ACTIVE);
        entity.setPublished(false);
        entity.setRedeemed(false);
        entity.setDeleted(false);
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }

    @Test
    @DisplayName("create() should persist and return response")
    void createShouldPersistAndReturn() {
        Coupon domain = buildDomain();
        CouponEntity entity = buildEntity();

        when(mapper.toEntity(any(Coupon.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        CreateCouponRequest req = new CreateCouponRequest("ABC123", "desc", new BigDecimal("1.0"), FUTURE, false);
        CouponResponse response = service.create(req);

        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("ABC123");
        assertThat(response.status()).isEqualTo(CouponStatus.ACTIVE);
        verify(repository, times(1)).save(entity);
    }

    @Test
    @DisplayName("findById() should return coupon when found")
    void findByIdShouldReturnCoupon() {
        Coupon domain = buildDomain();
        CouponEntity entity = buildEntity();
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        CouponResponse response = service.findById(id);

        assertThat(response.code()).isEqualTo("ABC123");
    }

    @Test
    @DisplayName("findById() should throw when not found")
    void findByIdShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    @DisplayName("delete() should call softDelete and save")
    void deleteShouldSoftDelete() {
        Coupon domain = buildDomain();
        CouponEntity entity = buildEntity();
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        service.delete(id);

        assertThat(domain.isDeleted()).isTrue();
        verify(repository, times(1)).save(entity);
    }

    @Test
    @DisplayName("delete() should throw when coupon not found")
    void deleteShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    @DisplayName("delete() should throw when coupon already deleted")
    void deleteShouldThrowWhenAlreadyDeleted() {
        Coupon domain = buildDomain();
        domain.softDelete();
        CouponEntity entity = buildEntity();
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(CouponAlreadyDeletedException.class);
    }
}
