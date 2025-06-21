package com.foodApp.repository;

import com.foodApp.model.MenuItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepository {
    MenuItem save(MenuItem menuItem);
    Optional<MenuItem> findById(Integer id);
    List<MenuItem> findByRestaurantId(Integer restaurantId);
    List<MenuItem> findAllByCategory(Integer categoryId);
    void delete(MenuItem item);
    List<MenuItem> findItemsWithFilters(String search, BigDecimal maxPrice, List<String> keywords); // متد جدید
}