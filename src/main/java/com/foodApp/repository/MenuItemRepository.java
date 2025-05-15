package com.foodApp.repository;

import com.foodApp.model.MenuItem;

import java.util.List;

public interface MenuItemRepository {
    void save(MenuItem menuItem);
    MenuItem findById(String id);
    List<MenuItem> findAllByRestaurant(int restaurantId);
    List<MenuItem> findAllByCategory(int categoryId);
    void delete(int id);
}
