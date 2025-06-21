package com.foodApp.service;

import com.foodApp.dto.ItemDto;
import com.foodApp.dto.OrderDto;
import com.foodApp.exception.OrderNotFoundException;
import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.exception.UserNotFoundException;
import com.foodApp.model.Order;
import com.foodApp.model.OrderItem;
import com.foodApp.model.OrderStatus;
import com.foodApp.model.MenuItem;
import com.foodApp.model.Restaurant;
import com.foodApp.model.User;
import com.foodApp.repository.OrderRepository;
import com.foodApp.repository.OrderRepositoryImpl;
import com.foodApp.repository.UserRepositoryImp;
import com.foodApp.repository.RestaurantRepositoryImp;
import com.foodApp.repository.MenuItemRepositoryImp;
import com.foodApp.repository.MenuItemRepository;
import com.foodApp.repository.RestaurantRepository;
import com.foodApp.repository.UserRepository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository = new OrderRepositoryImpl();
    private final UserRepository userRepository = new UserRepositoryImp();
    private final RestaurantRepository restaurantRepository = new RestaurantRepositoryImp();
    private final MenuItemRepository menuItemRepository = new MenuItemRepositoryImp();


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
        BigDecimal totalCalculatedPrice = BigDecimal.ZERO;

        // 3. Process each item in the order
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
            orderItem.setPriceAtOrder(menuItem.getPrice()); // Use the price from the MenuItem

            orderItems.add(orderItem);

            totalCalculatedPrice = totalCalculatedPrice.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalCalculatedPrice);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
        return order;
    }

    @Override
    public List<Order> findAllOrdersWithFilters(String search, String vendorName, String courierName, String customerName, OrderStatus status) {
        return orderRepository.findOrdersWithFilters(search, vendorName, courierName, customerName, status);
    }
}