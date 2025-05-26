package com.foodApp.service;

import com.foodApp.model.Restaurant;

import java.util.List;

public interface RestaurantService {
    Restaurant registerRestaurantAndReturn(Restaurant restaurant);
    void approveRestaurant(int  restaurantId);
    void deleteRestaurant(int  restaurantId);
    Restaurant updateRestaurantAndReturn(Restaurant restaurant);
    List<Restaurant> getAllApprovedRestaurants();
    List<Restaurant> getRestaurantByOwnerId(int OwnerId);
}
