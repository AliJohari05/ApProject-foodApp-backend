package com.foodApp.service;

import com.foodApp.model.Rating;
import com.foodApp.dto.RatingRequestDto;
import com.foodApp.dto.RatingResponseDto;
import com.foodApp.dto.ItemRatingsSummaryDto;
import java.util.List;

public interface RatingService {
    RatingResponseDto submitRating(Integer userId, RatingRequestDto requestDto);
    ItemRatingsSummaryDto getItemRatings(Integer menuItemId);
    RatingResponseDto getRatingById(Integer ratingId);
    RatingResponseDto updateRating(Integer ratingId, Integer userId, RatingRequestDto requestDto);
    void deleteRating(Integer ratingId, Integer userId);
}