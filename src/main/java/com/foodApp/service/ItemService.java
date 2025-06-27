package com.foodApp.service;

import com.foodApp.model.MenuItem;
import com.foodApp.dto.MenuItemCreateUpdateDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ItemService {
    List<MenuItem> findItemsWithFilters(String search, BigDecimal maxPrice, List<String> keywords);
    Optional<MenuItem> findItemById(Integer id);

    // New methods for restaurant-specific item management
    MenuItem createRestaurantMenuItem(Integer restaurantId, MenuItemCreateUpdateDto createDto);
    MenuItem updateRestaurantMenuItem(Integer restaurantId, Integer menuItemId, MenuItemCreateUpdateDto updateDto);
    void deleteRestaurantMenuItem(Integer restaurantId, Integer menuItemId);
}