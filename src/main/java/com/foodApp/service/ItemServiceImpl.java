package com.foodApp.service;

import com.foodApp.model.MenuItem;
import com.foodApp.repository.MenuItemRepository;
import com.foodApp.repository.MenuItemRepositoryImp;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ItemServiceImpl implements ItemService {
    private final MenuItemRepository menuItemRepository = new MenuItemRepositoryImp();

    @Override
    public List<MenuItem> findItemsWithFilters(String search, BigDecimal maxPrice, List<String> keywords) {
        // این متد MenuItemRepository.findItemsWithFilters را فراخوانی خواهد کرد
        return menuItemRepository.findItemsWithFilters(search, maxPrice, keywords);
    }

    @Override
    public Optional<MenuItem> findItemById(Integer id) {
        return menuItemRepository.findById(id);
    }
}