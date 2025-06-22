package com.foodApp.service;

import com.foodApp.model.Coupon;
import java.math.BigDecimal;
import java.util.Optional;

public interface CouponService {
    Optional<Coupon> validateAndGetCoupon(String couponCode, BigDecimal orderTotalPrice, Integer userId);
    void applyCoupon(Coupon coupon);
}