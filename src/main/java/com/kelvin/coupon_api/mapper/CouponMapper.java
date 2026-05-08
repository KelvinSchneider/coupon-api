package com.kelvin.coupon_api.mapper;

import com.kelvin.coupon_api.domain.Coupon;
import com.kelvin.coupon_api.persistence.CouponEntity;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

    public CouponEntity toEntity(Coupon coupon) {
        CouponEntity entity = new CouponEntity();
        entity.setId(coupon.getId());
        entity.setCode(coupon.getCode());
        entity.setDescription(coupon.getDescription());
        entity.setDiscountValue(coupon.getDiscountValue());
        entity.setExpirationDate(coupon.getExpirationDate());
        entity.setStatus(coupon.getStatus());
        entity.setPublished(coupon.isPublished());
        entity.setRedeemed(coupon.isRedeemed());
        entity.setDeleted(coupon.isDeleted());
        entity.setCreatedAt(coupon.getCreatedAt());
        entity.setDeletedAt(coupon.getDeletedAt());
        return entity;
    }

    public Coupon toDomain(CouponEntity entity) {
        return Coupon.reconstitute(
                entity.getId(),
                entity.getCode(),
                entity.getDescription(),
                entity.getDiscountValue(),
                entity.getExpirationDate(),
                entity.getStatus(),
                entity.isPublished(),
                entity.isRedeemed(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }
}