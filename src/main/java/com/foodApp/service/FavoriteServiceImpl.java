package com.foodApp.service;

import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.exception.UserNotFoundException;
import com.foodApp.model.Restaurant;
import com.foodApp.model.User;
import com.foodApp.repository.*;
import com.foodApp.util.Message;

import java.util.List;

public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository = new FavoriteRepositoryImpl();
    private final UserRepository userRepository = new UserRepositoryImp();
    private final RestaurantRepository restaurantRepository = new RestaurantRepositoryImp();
    @Override
    public List<Restaurant> getUserFavoriteRestaurants(int userId) {
        // Checking the existence of the user
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException(Message.USER_NOT_FOUND.get());
        }
        return favoriteRepository.findFavoriteRestaurantsByUserId(userId);
    }

    @Override
    public void addFavoriteRestaurant(int userId, int restaurantId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException(Message.USER_NOT_FOUND.get());
        }
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }
        if (favoriteRepository.isFavorite(userId, restaurantId)) {
            throw new IllegalArgumentException(Message.CONFLICT.get());
        }
        favoriteRepository.addFavorite(userId, restaurantId);

    }

    @Override
    public void removeFavoriteRestaurant(int userId, int restaurantId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException(Message.USER_NOT_FOUND.get());
        }
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }

        if (!favoriteRepository.isFavorite(userId, restaurantId)) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }

        favoriteRepository.removeFavorite(userId, restaurantId);
    }
}
