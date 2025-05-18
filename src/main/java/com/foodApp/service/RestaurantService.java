package com.foodApp.service;

import com.foodApp.model.Restaurant;

import java.util.List;

public interface RestaurantService {
    void registerRestaurant(Restaurant restaurant);
    void approveRestaurant(int  restaurantId);
    void deleteRestaurant(int  restaurantId);
    void updateRestaurant(Restaurant restaurant);
    List<Restaurant> getAllApprovedRestaurants();
    List<Restaurant> getRestaurantByOwnerId(int OwnerId);
}
