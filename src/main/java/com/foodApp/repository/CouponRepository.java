package com.foodApp.repository;

import com.foodApp.model.Coupon;
import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findByCouponCode(String couponCode);
    void save(Coupon coupon);
}