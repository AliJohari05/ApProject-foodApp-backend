package com.foodApp.service;

import com.foodApp.dto.CouponDto;
import com.foodApp.exception.CouponNotFoundException;
import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.model.Coupon;
import com.foodApp.model.CouponType;
import com.foodApp.repository.CouponRepository;
import com.foodApp.repository.CouponRepositoryImpl;
import com.foodApp.util.Message;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
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
        if (coupon.getUserCount() > 0) {
            coupon.setUserCount(coupon.getUserCount() - 1);
        }
        // FIX: couponRepository.save should now handle merge logic if it's an existing entity
        couponRepository.save(coupon); // Rely on the updated save method in repository
    }

    @Override
    public List<Coupon> findAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon createCoupon(CouponDto createDto) {
        if (createDto.getCouponCode() == null || createDto.getCouponCode().isBlank() ||
                createDto.getType() == null || createDto.getValue() == null ||
                createDto.getMinPrice() == null || createDto.getUserCount() == null ||
                createDto.getStartDate() == null || createDto.getEndDate() == null) {
            throw new IllegalArgumentException(Message.MISSING_FIELDS.get());
        }

        if (couponRepository.findByCouponCode(createDto.getCouponCode()).isPresent()) {
            throw new IllegalArgumentException(Message.CONFLICT.get());
        }

        LocalDateTime startDate, endDate;
        try {
            startDate = LocalDate.parse(createDto.getStartDate()).atStartOfDay();
            endDate = LocalDate.parse(createDto.getEndDate()).atTime(23, 59, 59);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(Message.INVALID_INPUT.get() + ": Invalid date format. UseYYYY-MM-DD.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(Message.INVALID_INPUT.get() + ": Start date cannot be after end date.");
        }

        Coupon coupon = new Coupon();
        coupon.setCouponCode(createDto.getCouponCode());
        coupon.setType(CouponType.valueOf(createDto.getType().toUpperCase())); // Convert String to Enum
        coupon.setValue(createDto.getValue());
        coupon.setMinPrice(createDto.getMinPrice());
        coupon.setUserCount(createDto.getUserCount());
        coupon.setStartDate(startDate);
        coupon.setEndDate(endDate);
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());

        couponRepository.save(coupon); // This save will handle new entity persist
        return coupon;
    }

    @Override
    public Optional<Coupon> getCouponById(int couponId) {
        return couponRepository.findById(couponId);
    }

    @Override
    public Coupon updateCoupon(int couponId, CouponDto updateDto) {
        Coupon existingCoupon = couponRepository.findById(couponId).orElse(null);
        if (existingCoupon == null) {
            throw new CouponNotFoundException(Message.ERROR_404.get()); // Using RestaurantNotFound, consider renaming exception
        }

        // Update fields if provided
        if (updateDto.getCouponCode() != null && !updateDto.getCouponCode().isBlank()) {
            if (!updateDto.getCouponCode().equals(existingCoupon.getCouponCode()) &&
                    couponRepository.findByCouponCode(updateDto.getCouponCode()).isPresent()) {
                throw new IllegalArgumentException(Message.CONFLICT.get());
            }
            existingCoupon.setCouponCode(updateDto.getCouponCode());
        }
        if (updateDto.getType() != null && !updateDto.getType().isBlank()) {
            existingCoupon.setType(CouponType.valueOf(updateDto.getType().toUpperCase()));
        }
        if (updateDto.getValue() != null) {
            existingCoupon.setValue(updateDto.getValue());
        }
        if (updateDto.getMinPrice() != null) {
            existingCoupon.setMinPrice(updateDto.getMinPrice());
        }
        if (updateDto.getUserCount() != null) {
            existingCoupon.setUserCount(updateDto.getUserCount());
        }

        if (updateDto.getStartDate() != null || updateDto.getEndDate() != null) {
            LocalDateTime startDate = existingCoupon.getStartDate();
            LocalDateTime endDate = existingCoupon.getEndDate();
            try {
                if (updateDto.getStartDate() != null) {
                    startDate = LocalDate.parse(updateDto.getStartDate()).atStartOfDay();
                }
                if (updateDto.getEndDate() != null) {
                    endDate = LocalDate.parse(updateDto.getEndDate()).atTime(23, 59, 59);
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(Message.INVALID_INPUT.get() + ": Invalid date format. UseYYYY-MM-DD.");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException(Message.INVALID_INPUT.get() + ": Start date cannot be after end date.");
            }
            existingCoupon.setStartDate(startDate);
            existingCoupon.setEndDate(endDate);
        }

        existingCoupon.setUpdatedAt(LocalDateTime.now());
        couponRepository.save(existingCoupon); // FIX: This save should now handle merging the detached entity
        return existingCoupon;

    }

    @Override
    public void deleteCoupon(Integer id) {
        Optional<Coupon> couponOptional = couponRepository.findById(id);
        if (couponOptional.isEmpty()) {
            throw new CouponNotFoundException(Message.ERROR_404.get()); // Using CouponNotFoundException
        }
        couponRepository.deleteById(id);
    }
}