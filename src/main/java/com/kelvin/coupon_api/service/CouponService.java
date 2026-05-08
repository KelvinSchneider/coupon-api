package com.kelvin.coupon_api.service;

import com.kelvin.coupon_api.domain.Coupon;
import com.kelvin.coupon_api.dto.request.CreateCouponRequest;
import com.kelvin.coupon_api.dto.response.CouponResponse;
import com.kelvin.coupon_api.exception.CouponNotFoundException;
import com.kelvin.coupon_api.mapper.CouponMapper;
import com.kelvin.coupon_api.persistence.CouponEntity;
import com.kelvin.coupon_api.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository repository;
    private final CouponMapper mapper;

    @Transactional
    public CouponResponse create(CreateCouponRequest request) {
        Coupon coupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published()
        );
        CouponEntity saved = repository.save(mapper.toEntity(coupon));
        return toResponse(mapper.toDomain(saved));
    }

    @Transactional(readOnly = true)
    public CouponResponse findById(UUID id) {
        return toResponse(getDomain(id));
    }

    @Transactional
    public void delete(UUID id) {
        Coupon coupon = getDomain(id);
        coupon.softDelete();
        repository.save(mapper.toEntity(coupon));
    }

    private Coupon getDomain(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new CouponNotFoundException(id.toString()));
    }

    private CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountValue(),
                coupon.getExpirationDate(),
                coupon.getStatus(),
                coupon.isPublished(),
                coupon.isRedeemed()
        );
    }
}
