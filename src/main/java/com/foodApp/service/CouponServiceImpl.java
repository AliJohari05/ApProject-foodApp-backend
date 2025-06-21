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
            return Optional.empty(); // کوپن یافت نشد
        }

        Coupon coupon = couponOptional.get();
        LocalDateTime now = LocalDateTime.now();

        // 1. بررسی تاریخ
        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
            return Optional.empty(); // کوپن منقضی شده یا هنوز فعال نشده است
        }

        // 2. بررسی حداقل قیمت سفارش (اگر orderTotalPrice ارائه شده باشد)
        if (orderTotalPrice != null && orderTotalPrice.compareTo(coupon.getMinPrice()) < 0) {
            return Optional.empty(); // قیمت سفارش از حداقل قیمت کوپن کمتر است
        }

        // 3. بررسی تعداد استفاده (برای این API فقط اعتبار اولیه را بررسی می‌کنیم)
        // منطق کاهش تعداد استفاده یا بررسی تعداد استفاده هر کاربر باید در زمان ثبت سفارش نهایی (order submission) انجام شود، نه در این API بررسی اعتبار اولیه.
        // پس اگر کوپن از نظر تاریخ و حداقل قیمت معتبر باشد، آن را برمی‌گردانیم.

        return Optional.of(coupon); // کوپن معتبر است
    }
}