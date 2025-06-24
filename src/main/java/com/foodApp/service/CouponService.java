package com.foodApp.service;

import com.foodApp.dto.CouponDto;
import com.foodApp.model.Coupon;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CouponService {
    Optional<Coupon> validateAndGetCoupon(String couponCode, BigDecimal orderTotalPrice, Integer userId);
    void applyCoupon(Coupon coupon);
    List<Coupon> findAllCoupons();

    Coupon createCoupon(CouponDto createDto);

    Optional<Coupon> getCouponById(int couponId);

    Coupon updateCoupon(int couponId, CouponDto updateDto);

    void deleteCoupon(Integer id);
}