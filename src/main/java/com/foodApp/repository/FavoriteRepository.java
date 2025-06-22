package com.foodApp.repository;

import com.foodApp.model.Restaurant;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository {
    void addFavorite(int userId, int restaurantId);
    void removeFavorite(int userId, int restaurantId);
    boolean isFavorite(int userId, int restaurantId);
    List<Restaurant> findFavoriteRestaurantsByUserId(int userId);
}