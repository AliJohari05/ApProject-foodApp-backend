package com.foodApp.service;

import com.foodApp.dto.ItemDto;
import com.foodApp.dto.OrderDto;
import com.foodApp.exception.*;
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
import com.foodApp.util.Message;


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
        BigDecimal rawCalculatedPrice = BigDecimal.ZERO;
        for (ItemDto itemDto : orderDto.getItems()) {
            Optional<MenuItem> menuItemOptional = menuItemRepository.findById(itemDto.getItemId());
            if (!menuItemOptional.isPresent()) {
                throw new OrderNotFoundException("Menu item with ID " + itemDto.getItemId() + " not found.");
            }
            MenuItem menuItem = menuItemOptional.get();

            if (menuItem.getStock() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for item: " + menuItem.getName());
            }
            menuItem.setStock(menuItem.getStock() - itemDto.getQuantity());
            menuItemRepository.save(menuItem);

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPriceAtOrder(menuItem.getPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);

            rawCalculatedPrice = rawCalculatedPrice.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        }
        order.setOrderItems(orderItems);
        order.setRawPrice(rawCalculatedPrice);

        BigDecimal taxRate = BigDecimal.valueOf(restaurant.getTaxFee()).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal taxFee = taxRate.multiply(rawCalculatedPrice);
        BigDecimal additionalFee = BigDecimal.valueOf(restaurant.getAdditionalFee());
        BigDecimal courierFee = BigDecimal.valueOf(5000);

        order.setTaxFee(taxFee);
        order.setAdditionalFee(additionalFee);
        order.setCourierFee(courierFee);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal preCouponTotalPrice = rawCalculatedPrice.add(taxFee).add(additionalFee).add(courierFee);
        BigDecimal finalTotalPrice = preCouponTotalPrice;

        if (orderDto.getCouponId() != null) {
            Optional<Coupon> foundCouponOptional = couponRepository.findById(orderDto.getCouponId());

            if (foundCouponOptional.isPresent()) {
                Coupon appliedCoupon = foundCouponOptional.get();

                if (couponService.validateAndGetCoupon(appliedCoupon.getCouponCode(), preCouponTotalPrice, customerId).isPresent()) {

                    if (appliedCoupon.getType() == CouponType.FIXED) {
                        finalTotalPrice = finalTotalPrice.subtract(appliedCoupon.getValue());
                    } else if (appliedCoupon.getType() == CouponType.PERCENT) {
                        BigDecimal discountAmount = preCouponTotalPrice.multiply(appliedCoupon.getValue().divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP));
                        finalTotalPrice = finalTotalPrice.subtract(discountAmount);
                    }
                    order.setCoupon(appliedCoupon);
                    couponService.applyCoupon(appliedCoupon);
                } else {
                    throw new IllegalArgumentException("Invalid or inapplicable coupon for this order.");
                }
            } else {
                throw new IllegalArgumentException("Coupon not found with ID: " + orderDto.getCouponId());
            }
        }

        if (finalTotalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalTotalPrice = BigDecimal.ZERO;
        }

        order.setTotalPrice(finalTotalPrice);

        orderRepository.save(order);
        return order;
    }

    @Override
    public List<Order> findAllOrdersWithFilters(String search, String vendorName, String courierName, String customerName, OrderStatus status) {
        return orderRepository.findOrdersWithFilters(search, vendorName, courierName, customerName, status);
    }

    @Override
    public List<Order> getRestaurantOrders(Integer restaurantId, String search, String customerName, String courierName, OrderStatus status) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }

        // هیچ تبدیل اضافی انجام نده، همون status رو پاس بده به repository
        return orderRepository.findOrdersByRestaurantIdWithFilters(restaurantId, search, customerName, courierName, status);
    }




    @Override
    public void updateOrderStatusByRestaurant( Integer orderId, String newStatusString) {


        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(Message.ERROR_404.get());
        }


        OrderStatus newStatus;
        switch (newStatusString.toLowerCase()) {
            case "accepted": newStatus = OrderStatus.ACCEPTED_BY_VENDOR; break;
            case "rejected": newStatus = OrderStatus.REJECTED_BY_VENDOR; break;
            case "served": newStatus = OrderStatus.READY_FOR_PICKUP; break; // "served" -> آماده برای تحویل
            default: throw new IllegalArgumentException("Invalid status provided for order update.");
        }

        if (order.getStatus() == OrderStatus.DELIVERED_TO_CUSTOMER || order.getStatus() == OrderStatus.CANCELLED_BY_USER || order.getStatus() == OrderStatus.CANCELLED_BY_VENDOR) {
            throw new InvalidDeliveryStatusTransitionException("Cannot change status of a completed or cancelled order.");
        }
        // PENDING -> ACCEPTED/REJECTED
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.WAITING_VENDOR_ACCEPTANCE) {
            if (newStatus == OrderStatus.ACCEPTED_BY_VENDOR || newStatus == OrderStatus.REJECTED_BY_VENDOR) {
                order.setStatus(newStatus);
            } else {
                throw new InvalidDeliveryStatusTransitionException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
            }
        }
        // ACCEPTED -> PREPARING -> READY_FOR_PICKUP (served)
        else if (order.getStatus() == OrderStatus.ACCEPTED_BY_VENDOR || order.getStatus() == OrderStatus.PREPARING) {
            if (newStatus == OrderStatus.READY_FOR_PICKUP) { // فقط "served" را پس از accepted/preparing مجاز می‌دانیم
                order.setStatus(newStatus);
            } else {
                throw new InvalidDeliveryStatusTransitionException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
            }
        }
        else {
            throw new InvalidDeliveryStatusTransitionException("Order status cannot be changed from " + order.getStatus());
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public List<Order> getBuyerOrderHistory(int customerId, String search, String vendorName) {
        return orderRepository.findOrdersByCustomerIdWithFilters(customerId, search, vendorName);
    }

}