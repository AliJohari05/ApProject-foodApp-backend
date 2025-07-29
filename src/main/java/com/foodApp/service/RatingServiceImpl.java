package com.foodApp.service;

import com.foodApp.dto.RatingRequestDto;
import com.foodApp.dto.RatingResponseDto;
import com.foodApp.dto.ItemRatingsSummaryDto;
import com.foodApp.exception.OrderNotFoundException;
import com.foodApp.exception.UserNotFoundException;
import com.foodApp.exception.MenuItemNotFoundException;
import com.foodApp.exception.UnauthorizedAccessException;
import com.foodApp.model.Rating;
import com.foodApp.model.User;
import com.foodApp.model.Order;
import com.foodApp.model.MenuItem;
import com.foodApp.repository.RatingRepository;
import com.foodApp.repository.RatingRepositoryImpl;
import com.foodApp.repository.UserRepository;
import com.foodApp.repository.UserRepositoryImp;
import com.foodApp.repository.OrderRepository;
import com.foodApp.repository.OrderRepositoryImpl;
import com.foodApp.repository.MenuItemRepository;
import com.foodApp.repository.MenuItemRepositoryImp;
import com.foodApp.util.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository = new RatingRepositoryImpl();
    private final UserRepository userRepository = new UserRepositoryImp();
    private final OrderRepository orderRepository = new OrderRepositoryImpl();
    private final MenuItemRepository menuItemRepository = new MenuItemRepositoryImp();

    @Override
    public RatingResponseDto submitRating(Integer userId, RatingRequestDto requestDto) {
        if (requestDto.getOrderId() == null ||  requestDto.getRating() == null || requestDto.getComment() == null) {
            throw new IllegalArgumentException(Message.MISSING_FIELDS.get()); //requestDto.getItemId() == null ||
        }
        if (requestDto.getRating() < 1 || requestDto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException(Message.USER_NOT_FOUND.get());
        }
        Order order = orderRepository.findById(requestDto.getOrderId());
        if (order == null) {
            throw new OrderNotFoundException(Message.ERROR_404.get());
        }
        // The user must be a customer of this order.
        if (order.getCustomer().getUserId() != userId) {
            throw new UnauthorizedAccessException("User is not authorized to rate this order."); //
        }

//        MenuItem menuItem = menuItemRepository.findById(requestDto.getItemId()).orElse(null);
//        if (menuItem == null) {
//            throw new MenuItemNotFoundException(Message.ERROR_404.get());
//        }

        // Check if the user has already rated this item in this particular order
        Optional<Rating> existingRating = ratingRepository.findByUserIdAndOrderIdAndMenuItemId(userId, requestDto.getOrderId());
        if (existingRating.isPresent()) {
            throw new IllegalArgumentException(Message.CONFLICT.get() + ": User has already rated this item in this order."); //
        }

        Rating rating = new Rating();
        rating.setUser(user);
        rating.setOrder(order);
        //rating.setMenuItem(menuItem);
        rating.setRating(requestDto.getRating());
        rating.setComment(requestDto.getComment());
        rating.setCreatedAt(LocalDateTime.now());
        rating.setUpdatedAt(LocalDateTime.now());
        if (requestDto.getImageBase64() != null && !requestDto.getImageBase64().isEmpty()) {
            rating.setImageUrl(requestDto.getImageBase64().get(0));
        }

        Rating savedRating = ratingRepository.save(rating);
        return new RatingResponseDto(savedRating);
    }

    @Override
    public ItemRatingsSummaryDto getItemRatings(Integer menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId).orElse(null);
        if (menuItem == null) {
            throw new MenuItemNotFoundException(Message.ERROR_404.get());
        }

        List<Rating> ratings = ratingRepository.findByMenuItemId(menuItemId);
        Double avgRating = ratingRepository.findAverageRatingByMenuItemId(menuItemId);

        List<RatingResponseDto> comments = ratings.stream()
                .map(RatingResponseDto::new)
                .collect(Collectors.toList());

        return new ItemRatingsSummaryDto(avgRating != null ? avgRating : 0.0, comments);
    }

    @Override
    public RatingResponseDto getRatingById(Integer ratingId) {
        Rating rating = ratingRepository.findByOrderId(ratingId).orElse(null);
        if (rating == null) {
            throw new OrderNotFoundException(Message.ERROR_404.get());
        }
        return new RatingResponseDto(rating);
    }

    @Override
    public RatingResponseDto updateRating(Integer ratingId, Integer userId, RatingRequestDto requestDto) {
        if (requestDto.getRating() == null && requestDto.getComment() == null && (requestDto.getImageBase64() == null || requestDto.getImageBase64().isEmpty())) {
            throw new IllegalArgumentException(Message.INVALID_INPUT.get() + ": Nothing to update.");
        }
        if (requestDto.getRating() != null && (requestDto.getRating() < 1 || requestDto.getRating() > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        Rating existingRating = ratingRepository.findById(ratingId).orElse(null);
        if (existingRating == null) {
            throw new OrderNotFoundException(Message.ERROR_404.get());
        }
        if (existingRating.getUser().getUserId() != userId) {
            throw new UnauthorizedAccessException(Message.FORBIDDEN.get()); //
        }

        if (requestDto.getRating() != null) {
            existingRating.setRating(requestDto.getRating());
        }
        if (requestDto.getComment() != null) {
            existingRating.setComment(requestDto.getComment());
        }
        if (requestDto.getImageBase64() != null) {
            existingRating.setImageUrl(requestDto.getImageBase64().isEmpty() ? null : requestDto.getImageBase64().get(0));
        }

        Rating updatedRating = ratingRepository.save(existingRating);
        return new RatingResponseDto(updatedRating);
    }

    @Override
    public void deleteRating(Integer ratingId, Integer userId) {
        Rating existingRating = ratingRepository.findById(ratingId).orElse(null);
        if (existingRating == null) {
            throw new OrderNotFoundException(Message.ERROR_404.get());
        }
        // Only the user who registered the score can delete it.
        if (existingRating.getUser().getUserId() != userId) {
            throw new UnauthorizedAccessException(Message.FORBIDDEN.get()); //
        }
        ratingRepository.delete(existingRating);
    }
}