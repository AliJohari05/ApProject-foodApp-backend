package com.foodApp.service;

import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.model.Restaurant;
import com.foodApp.repository.RestaurantRepository;
import com.foodApp.repository.RestaurantRepositoryImp;

import java.util.List;

public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepo = new RestaurantRepositoryImp();
    @Override
    public void registerRestaurant(Restaurant restaurant) {
        restaurant.setApproved(false);
        restaurantRepo.save(restaurant);
    }

    @Override
    public void approveRestaurant(int restaurantId) {
        Restaurant restaurant = restaurantRepo.findById(restaurantId);
        if(restaurant != null) {
            restaurant.setApproved(true);
            restaurantRepo.save(restaurant);
        }

    }

    @Override
    public void deleteRestaurant(int restaurantId) {
        restaurantRepo.deleteById(restaurantId);
    }

    @Override
    public void updateRestaurant(Restaurant restaurant) {
        Restaurant existing = restaurantRepo.findById(restaurant.getId());
        if (existing != null) {
            restaurantRepo.save(restaurant);
        } else {
            throw new RestaurantNotFoundException("Restaurant not found");
        }

    }

    @Override
    public List<Restaurant> getAllApprovedRestaurants() {
        return restaurantRepo.findAllApproved();
    }

    @Override
    public List<Restaurant> getRestaurantByOwnerId(int OwnerId) {
        return restaurantRepo.findByOwnerId(OwnerId);
    }
}
