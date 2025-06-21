package com.foodApp.service;

import com.foodApp.model.Restaurant;

import com.foodApp.dto.RestaurantDto;
import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.exception.UnauthorizedAccessException;

import java.util.List;

public interface RestaurantService {
    Restaurant registerRestaurantAndReturn(Restaurant restaurant);
    void approveRestaurant(int  restaurantId);
    void deleteRestaurant(int  restaurantId);
    Restaurant findById(int  sellerId);
    Restaurant updateRestaurantAndReturn(Restaurant restaurant);
    Restaurant updateRestaurantForPut(int restaurantId, RestaurantDto restaurantDto, int ownerId) throws RestaurantNotFoundException, UnauthorizedAccessException;
    List<Restaurant> getAllApprovedRestaurants();
    List<Restaurant> getRestaurantByOwnerId(int OwnerId);
    List<Restaurant> findApprovedByFilters(String search, List<String> keywords);
}
