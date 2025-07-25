package com.foodApp.repository;

import com.foodApp.model.Rating;
import java.util.List;
import java.util.Optional;

public interface RatingRepository {
    Rating save(Rating rating);
    Optional<Rating> findById(Integer id);
    List<Rating> findByMenuItemId(Integer menuItemId);
    void delete(Rating rating);
//    Optional<Rating> findByUserIdAndOrderIdAndMenuItemId(int userId, int orderId, int menuItemId);
    Optional<Rating> findByUserIdAndOrderIdAndMenuItemId(int userId, int orderId);
    Double findAverageRatingByMenuItemId(Integer menuItemId);
}