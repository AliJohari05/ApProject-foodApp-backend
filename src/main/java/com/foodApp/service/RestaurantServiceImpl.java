package com.foodApp.service;

import com.foodApp.dto.RestaurantDto;
import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.exception.UnauthorizedAccessException;
import com.foodApp.model.Restaurant;
import com.foodApp.repository.RestaurantRepository;
import com.foodApp.repository.RestaurantRepositoryImp;
import com.foodApp.util.Message;
import java.util.List;

import java.time.LocalDateTime;

public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepo = new RestaurantRepositoryImp();
    @Override
    public Restaurant registerRestaurantAndReturn(Restaurant restaurant) {
        restaurant.setApproved(true);

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
    public Restaurant findById(int sellerId) {
        return restaurantRepo.findByIdSeller(sellerId);
    }

    @Override
    public Restaurant updateRestaurantAndReturn(Restaurant restaurantToUpdate) {
        Restaurant existingRestaurant = restaurantRepo.findById(restaurantToUpdate.getId());
        if (existingRestaurant != null) {
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
    public Restaurant updateRestaurantForPut(int restaurantId, RestaurantDto restaurantDto, int ownerId)
            throws RestaurantNotFoundException, UnauthorizedAccessException{
        Restaurant existingRestaurant = restaurantRepo.findById(restaurantId);
        if(ownerId != existingRestaurant.getOwner().getUserId()){
        throw new UnauthorizedAccessException(Message.UNAUTHORIZED.get());
        }
        if (restaurantDto.getName() != null && !restaurantDto.getName().isBlank()) {
            existingRestaurant.setName(restaurantDto.getName()); //
        }
        if (restaurantDto.getAddress() != null && !restaurantDto.getAddress().isBlank()) {
            existingRestaurant.setAddress(restaurantDto.getAddress()); //
        }
        String validationError = restaurantDto.validateFields(); // از متد موجود در DTO استفاده می‌کنیم
        if (restaurantDto.getPhone() != null && !restaurantDto.getPhone().isBlank()) {
            if (validationError != null && validationError.contains("invalid phone")) {
                throw new IllegalArgumentException(Message.INVALID_INPUT.get());
            }
            existingRestaurant.setPhone(restaurantDto.getPhone()); //
        }
        if (restaurantDto.getLogoBase64() != null) {
            existingRestaurant.setLogobase64(restaurantDto.getLogoBase64()); //
        }
        if (restaurantDto.getTax_fee() != null) {
            if (restaurantDto.getTax_fee() < 0) {
                // throw new IllegalArgumentException("Tax fee cannot be negative.");
            }
            existingRestaurant.setTaxFee(restaurantDto.getTax_fee()); //
        }
        if (restaurantDto.getAdditional_fee() != null) {
            if (restaurantDto.getAdditional_fee() < 0) {
                throw new IllegalArgumentException(Message.INVALID_INPUT.get());
            }
            existingRestaurant.setAdditionalFee(restaurantDto.getAdditional_fee()); //
        }
        existingRestaurant.setUpdatedAt(java.time.LocalDateTime.now()); //
        return restaurantRepo.save(existingRestaurant);
    }
    @Override
    public List<Restaurant> getAllApprovedRestaurants() {
        return restaurantRepo.findAllApproved();
    }

    @Override
    public List<Restaurant> getRestaurantByOwnerId(int OwnerId) {
        return restaurantRepo.findByOwnerId(OwnerId);
    }

    @Override
    public List<Restaurant> findApprovedByFilters(String search, List<String> keywords) {
        return restaurantRepo.findApprovedByFilters(search, keywords);
    }
}
