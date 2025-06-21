package com.foodApp.service;

import com.foodApp.model.MenuItem;
import java.math.BigDecimal; // For price filtering
import java.util.List;
import java.util.Optional; // For findById

public interface ItemService {
    List<MenuItem> findItemsWithFilters(String search, BigDecimal maxPrice, List<String> keywords);
    Optional<MenuItem> findItemById(Integer id);
}