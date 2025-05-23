package com.foodApp.repository;

import com.foodApp.model.MenuItem;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository {
    MenuItem save(MenuItem menuItem);
    Optional<MenuItem> findById(Integer id);
    List<MenuItem> findByRestaurantId(Integer restaurantId);
    List<MenuItem> findAllByCategory(Integer categoryId);
    void delete(MenuItem item);
}
