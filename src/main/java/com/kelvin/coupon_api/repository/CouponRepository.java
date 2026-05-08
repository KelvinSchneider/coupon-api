package com.kelvin.coupon_api.repository;

import com.kelvin.coupon_api.persistence.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {

    Optional<CouponEntity> findById(UUID id);

}
