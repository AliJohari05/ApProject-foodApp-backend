package com.foodApp.repository;

import com.foodApp.model.Coupon;
import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findByCouponCode(String couponCode);
    void save(Coupon coupon);
    void updateUsageCount(Coupon coupon); // New: To decrement user_count
    Optional<Coupon> findById(Integer id); // New: To find coupon by ID
}
