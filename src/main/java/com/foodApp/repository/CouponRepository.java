package com.foodApp.repository;

import com.foodApp.model.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findByCouponCode(String couponCode);
    void save(Coupon coupon);
    void updateUsageCount(Coupon coupon);
    Optional<Coupon> findById(Integer id);
    List<Coupon> findAll();
    void deleteById(Integer id);
}
