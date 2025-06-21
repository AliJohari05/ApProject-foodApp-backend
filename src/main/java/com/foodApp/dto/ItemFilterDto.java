package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ItemFilterDto {
    private String search;
    private Integer price; // This represents max price as per common filter patterns
    private List<String> keywords;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}