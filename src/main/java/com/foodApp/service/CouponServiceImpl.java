package com.foodApp.service;

import com.foodApp.model.Coupon;
import com.foodApp.repository.CouponRepository;
import com.foodApp.repository.CouponRepositoryImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository = new CouponRepositoryImpl();

    @Override
    public Optional<Coupon> validateAndGetCoupon(String couponCode, BigDecimal orderTotalPrice, Integer userId) {
        Optional<Coupon> couponOptional = couponRepository.findByCouponCode(couponCode);

        if (couponOptional.isEmpty()) {
            return Optional.empty();
        }

        Coupon coupon = couponOptional.get();
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
            return Optional.empty();
        }

        if (orderTotalPrice != null && orderTotalPrice.compareTo(coupon.getMinPrice()) < 0) {
            return Optional.empty();
        }

        return Optional.of(coupon);
    }

    @Override
    public void applyCoupon(Coupon coupon) { // New method implementation
        if (coupon.getUserCount() > 0) { // اگر user_count یک محدودیت سراسری باشد
            coupon.setUserCount(coupon.getUserCount() - 1);
        }
        couponRepository.updateUsageCount(coupon);
    }
}