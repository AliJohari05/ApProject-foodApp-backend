package com.foodApp.dto;

import java.util.List;

public class VendorFilterDto {
    private String search;
    private List<String> keywords;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
