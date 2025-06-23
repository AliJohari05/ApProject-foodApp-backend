package com.foodApp.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public class RatingRequestDto {
    @JsonProperty("order_id")
    private Integer orderId;
    @JsonProperty("item_id")
    private Integer itemId;
    private Integer rating;
    private String comment;
    @JsonProperty("imageBase64")
    private List<String> imageBase64;

// getter and setter

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(List<String> imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
