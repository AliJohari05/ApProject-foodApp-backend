package com.foodApp.service;

import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.model.Restaurant;
import com.foodApp.repository.RestaurantRepository;
import com.foodApp.repository.RestaurantRepositoryImp;

import java.util.List;

import java.time.LocalDateTime;

public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepo = new RestaurantRepositoryImp();
    @Override
    public Restaurant registerRestaurantAndReturn(Restaurant restaurant) {
        restaurant.setApproved(false);

        return restaurantRepo.save(restaurant);
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
    public Restaurant updateRestaurantAndReturn(Restaurant restaurantToUpdate) {
        Restaurant existingRestaurant = restaurantRepo.findById(restaurantToUpdate.getId());
        if (existingRestaurant != null) {
            // کپی کردن فیلدهای قابل تغییر از restaurantToUpdate به existingRestaurant
            // Handler مسئول بررسی این است که کدام فیلدها اجازه آپدیت دارند
            if (restaurantToUpdate.getName() != null) {
                existingRestaurant.setName(restaurantToUpdate.getName());
            }
            if (restaurantToUpdate.getAddress() != null) {
                existingRestaurant.setAddress(restaurantToUpdate.getAddress());
            }
            if (restaurantToUpdate.getPhone() != null) {
                existingRestaurant.setPhone(restaurantToUpdate.getPhone());
            }
            if (restaurantToUpdate.getLogobase64() != null) {
                existingRestaurant.setLogobase64(restaurantToUpdate.getLogobase64());
            }
            if (restaurantToUpdate.getTaxFee() != null) {
                existingRestaurant.setTaxFee(restaurantToUpdate.getTaxFee());
            }
            if (restaurantToUpdate.getAdditionalFee() != null) {
                existingRestaurant.setAdditionalFee(restaurantToUpdate.getAdditionalFee());
            }
            existingRestaurant.setUpdatedAt(LocalDateTime.now());
            return restaurantRepo.save(existingRestaurant);
        } else {
            throw new RestaurantNotFoundException("Restaurant with ID " + restaurantToUpdate.getId() + " not found for update.");
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
