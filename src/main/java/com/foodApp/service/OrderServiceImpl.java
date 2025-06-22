package com.foodApp.service;

import com.foodApp.dto.ItemDto;
import com.foodApp.dto.OrderDto;
import com.foodApp.exception.OrderNotFoundException;
import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.exception.UserNotFoundException;
import com.foodApp.model.*;
import com.foodApp.repository.OrderRepository;
import com.foodApp.repository.OrderRepositoryImpl;
import com.foodApp.repository.UserRepositoryImp;
import com.foodApp.repository.RestaurantRepositoryImp;
import com.foodApp.repository.MenuItemRepositoryImp;
import com.foodApp.repository.MenuItemRepository;
import com.foodApp.repository.RestaurantRepository;
import com.foodApp.repository.UserRepository;
import com.foodApp.repository.CouponRepository; // Added for findById
import com.foodApp.repository.CouponRepositoryImpl; // Added for findById


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.foodApp.model.CouponType.FIXED;

public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository = new OrderRepositoryImpl();
    private final UserRepository userRepository = new UserRepositoryImp();
    private final RestaurantRepository restaurantRepository = new RestaurantRepositoryImp();
    private final MenuItemRepository menuItemRepository = new MenuItemRepositoryImp();
    private final CouponService couponService = new CouponServiceImpl(); // Injected
    private final CouponRepository couponRepository = new CouponRepositoryImpl(); // Added for finding coupon by ID


    @Override
    public void save(Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(int id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findOrdersByStatus(status);
    }

    @Override
    public void deleteById(int id) {
        orderRepository.deleteById(id);
    }

    @Override
    public Order submitOrder(OrderDto orderDto, int customerId) {
        // 1. Fetch Customer
        User customer = userRepository.findById(customerId);
        if (customer == null) {
            throw new UserNotFoundException("Customer with ID " + customerId + " not found.");
        }

        // 2. Fetch Restaurant
        Restaurant restaurant = restaurantRepository.findById(orderDto.getVendorId());
        if (restaurant == null) {
            throw new RestaurantNotFoundException("Restaurant with ID " + orderDto.getVendorId() + " not found.");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(orderDto.getDeliveryAddress());
        order.setStatus(OrderStatus.SUBMITTED); // Initial status

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal rawCalculatedPrice = BigDecimal.ZERO; // قیمت خام (قبل از مالیات، هزینه‌ها و کوپن)

        // 3. پردازش آیتم‌های سفارش برای محاسبه قیمت خام و پر کردن آیتم‌های سفارش
        for (ItemDto itemDto : orderDto.getItems()) {
            Optional<MenuItem> menuItemOptional = menuItemRepository.findById(itemDto.getItemId());
            if (!menuItemOptional.isPresent()) {
                throw new OrderNotFoundException("Menu item with ID " + itemDto.getItemId() + " not found.");
            }
            MenuItem menuItem = menuItemOptional.get();

            if (menuItem.getStock() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for item: " + menuItem.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPriceAtOrder(menuItem.getPrice());

            orderItems.add(orderItem);

            rawCalculatedPrice = rawCalculatedPrice.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        }
        order.setOrderItems(orderItems);
        order.setRawPrice(rawCalculatedPrice); // تنظیم قیمت خام

        // 4. محاسبه هزینه‌ها (مالیات، اضافی، پیک)
        // فرض می‌کنیم taxFee درصدی و additionalFee ثابت است. courierFee یک مقدار ثابت در نظر گرفته شده است.
        BigDecimal taxRate = BigDecimal.valueOf(restaurant.getTaxFee()).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal taxFee = taxRate.multiply(rawCalculatedPrice);
        BigDecimal additionalFee = BigDecimal.valueOf(restaurant.getAdditionalFee());
        BigDecimal courierFee = BigDecimal.valueOf(5000); // مبلغ ثابت برای هزینه پیک

        order.setTaxFee(taxFee);
        order.setAdditionalFee(additionalFee);
        order.setCourierFee(courierFee);

        // محاسبه قیمت کل قبل از اعمال کوپن
        BigDecimal preCouponTotalPrice = rawCalculatedPrice.add(taxFee).add(additionalFee).add(courierFee);
        BigDecimal finalTotalPrice = preCouponTotalPrice; // شروع با قیمت کل قبل از کوپن

        // 5. اعمال کوپن در صورت ارائه شدن
        if (orderDto.getCouponId() != null) {
            Optional<Coupon> foundCouponOptional = couponRepository.findById(orderDto.getCouponId());

            if (foundCouponOptional.isPresent()) {
                Coupon appliedCoupon = foundCouponOptional.get();

                // اعتبارسنجی کامل کوپن (تاریخ، حداقل قیمت، و غیره)
                // از متد validateAndGetCoupon در CouponService استفاده می‌کنیم که این اعتبارسنجی‌ها را انجام می‌دهد
                // اینجا orderTotalPrice و userId پاس داده می‌شوند.
                if (couponService.validateAndGetCoupon(appliedCoupon.getCouponCode(), preCouponTotalPrice, customerId).isPresent()) {

                    if (appliedCoupon.getType() == CouponType.FIXED) {
                        finalTotalPrice = finalTotalPrice.subtract(appliedCoupon.getValue());
                    } else if (appliedCoupon.getType() == CouponType.PERCENT) {
                        BigDecimal discountAmount = preCouponTotalPrice.multiply(appliedCoupon.getValue().divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP));
                        finalTotalPrice = finalTotalPrice.subtract(discountAmount);
                    }
                    order.setCoupon(appliedCoupon); // تنظیم کوپن اعمال شده
                    couponService.applyCoupon(appliedCoupon); // کاهش تعداد استفاده از کوپن
                } else {
                    // کوپن برای این سفارش نامعتبر است (مثلاً حداقل قیمت برآورده نشده، یا منقضی شده)
                    throw new IllegalArgumentException("Invalid or inapplicable coupon for this order.");
                }
            } else {
                // ID کوپن ارائه شده اما کوپن یافت نشد
                throw new IllegalArgumentException("Coupon not found with ID: " + orderDto.getCouponId());
            }
        }

        // اطمینان از اینکه قیمت نهایی منفی نشود
        if (finalTotalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalTotalPrice = BigDecimal.ZERO;
        }

        order.setTotalPrice(finalTotalPrice); // تنظیم قیمت نهایی برای پرداخت

        orderRepository.save(order);
        return order;
    }

    @Override
    public List<Order> findAllOrdersWithFilters(String search, String vendorName, String courierName, String customerName, OrderStatus status) {
        return orderRepository.findOrdersWithFilters(search, vendorName, courierName, customerName, status);
    }
}