package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodApp.model.Coupon;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class CouponDto {
    private Integer id;
    @JsonProperty("coupon_code")
    private String couponCode;
    private String type;
    private BigDecimal value;
    @JsonProperty("min_price")
    private BigDecimal minPrice;
    @JsonProperty("user_count")
    private Integer userCount;
    @JsonProperty("start_date")
    private String startDate;
    @JsonProperty("end_date")
    private String endDate;

    public CouponDto() {}

    public CouponDto(Coupon coupon) {
        this.id = coupon.getId();
        this.couponCode = coupon.getCouponCode();
        this.type = coupon.getType().name().toLowerCase();
        this.value = coupon.getValue();
        this.minPrice = coupon.getMinPrice();
        this.userCount = coupon.getUserCount();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
        this.startDate = coupon.getStartDate().toLocalDate().format(formatter);
        this.endDate = coupon.getEndDate().toLocalDate().format(formatter);
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}