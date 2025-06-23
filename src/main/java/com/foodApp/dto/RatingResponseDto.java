package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodApp.model.Rating;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Collections;

public class RatingResponseDto {
    private Integer id;
    @JsonProperty("order_id")
    private Integer orderId;
    @JsonProperty("item_id")
    private Integer itemId;
    private Integer rating;
    private String comment;
    @JsonProperty("imageBase64")
    private List<String> imageBase64;
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("created_at")
    private String createdAt;

    public RatingResponseDto() {}

    public RatingResponseDto(Rating rating) {
        this.id = rating.getId();
        this.orderId = rating.getOrder() != null ? rating.getOrder().getId() : null;
        this.itemId = rating.getMenuItem() != null ? rating.getMenuItem().getId() : null;
        this.rating = rating.getRating();
        this.comment = rating.getComment();
        this.imageBase64 = (rating.getImageUrl() != null && !rating.getImageUrl().isEmpty()) ?
                List.of(rating.getImageUrl()) : Collections.emptyList();
        this.userId = rating.getUser() != null ? rating.getUser().getUserId() : null;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        this.createdAt = rating.getCreatedAt() != null ? rating.getCreatedAt().format(formatter) : null;
    }

    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getImageBase64() { return imageBase64; }
    public void setImageBase64(List<String> imageBase64) { this.imageBase64 = imageBase64; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}