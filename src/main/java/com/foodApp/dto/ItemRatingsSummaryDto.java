package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ItemRatingsSummaryDto {
    @JsonProperty("avg_rating")
    private Double avgRating;
    private List<RatingResponseDto> comments;

    public ItemRatingsSummaryDto(Double avgRating, List<RatingResponseDto> comments) {
        this.avgRating = avgRating;
        this.comments = comments;
    }

    // Getters and Setters
    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
    public List<RatingResponseDto> getComments() { return comments; }
    public void setComments(List<RatingResponseDto> comments) { this.comments = comments; }
}