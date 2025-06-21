package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodApp.model.*;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderDto {

    @JsonProperty("delivery_address")
    private String deliveryAddress;
    @JsonProperty("vendor_id")
    private Integer vendorId;
    @JsonProperty("coupon_id")
    private Integer couponId;
    @JsonProperty("items")
    private List<ItemDto> items;


    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }
    public static Order toOrder(OrderDto dto) {
        Order order = new Order();
        order.setDeliveryAddress(dto.getDeliveryAddress());
        order.setTotalPrice(calculateTotalPrice(dto.getItems())); // فرض بر این است که متدی برای محاسبه قیمت کل وجود دارد
        // در اینجا باید customer و restaurant را هم تنظیم کنید
        // به عنوان مثال:
        // order.setCustomer(getCustomerById(customerId)); // نیاز به پیاده‌سازی این متد دارید
        // order.setRestaurant(getRestaurantById(dto.getVendorId())); // نیاز به پیاده‌سازی این متد دارید

        List<OrderItem> orderItems = new ArrayList<>();
        for (ItemDto itemDto : dto.getItems()) {
            OrderItem orderItem = new OrderItem(); // فرض بر این است که کلاس OrderItem وجود دارد
            orderItem.setItemId(itemDto.getItemId());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        return order;
    }

    private static BigDecimal calculateTotalPrice(List<ItemDto> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemDto item : items) {
            // فرض بر این است که متدی برای دریافت قیمت هر آیتم وجود دارد
            BigDecimal itemPrice = getItemPrice(item.getItemId());
            total = total.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    public boolean isValid() {
        return deliveryAddress != null && !deliveryAddress.isBlank()
                && vendorId != null;
    }
}
