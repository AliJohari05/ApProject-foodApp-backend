package com.foodApp.service;

import com.foodApp.model.Restaurant;
import java.util.List;

public interface FavoriteService {
    List<Restaurant> getUserFavoriteRestaurants(int userId);
    void addFavoriteRestaurant(int userId, int restaurantId);
    void removeFavoriteRestaurant(int userId, int restaurantId);
}