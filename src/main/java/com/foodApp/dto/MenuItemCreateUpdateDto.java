package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.regex.Pattern; // برای اعتبارسنجی (در متد validateFields)

public class MenuItemCreateUpdateDto {
    private String name;
    @JsonProperty("imageBase64")
    private String imageBase64;
    private String description;
    private Integer price; // طبق OpenAPI، از نوع Integer
    private Integer supply;
    private List<String> keywords;
//    @JsonProperty("vendor_id")
//    private Integer vendorId;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getSupply() { return supply; }
    public void setSupply(Integer supply) { this.supply = supply; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public boolean hasRequiredFieldsForCreate() {
        return name != null && !name.isBlank() &&
                description != null && !description.isBlank() &&
                price != null &&
                supply != null &&
                keywords != null && !keywords.isEmpty();
    }

    public String validateFields() {
        if (name != null && (name.length() < 2 || name.length() > 100)) {
            return "Invalid name: length must be between 2 and 100 characters.";
        }
        if (price != null && price < 0) {
            return "Invalid price: cannot be negative.";
        }
        if (supply != null && supply < 0) {
            return "Invalid supply: cannot be negative.";
        }
        if (keywords != null && keywords.stream().anyMatch(String::isBlank)) {
            return "Keywords cannot be blank.";
        }
        return null;
    }
}