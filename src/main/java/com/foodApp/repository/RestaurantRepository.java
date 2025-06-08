package com.foodApp.repository;

import com.foodApp.model.Restaurant;

import java.util.List;

public interface RestaurantRepository {
    Restaurant save(Restaurant restaurant);
    Restaurant findById(int id);
    List<Restaurant> findAllApproved();
    List<Restaurant> findApprovedByFilters(String search, List<String> keywords);
    List<Restaurant> findByOwnerId(int ownerId);
    void deleteById(int id);
}
