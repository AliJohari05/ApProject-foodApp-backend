package com.foodApp.service;

import com.foodApp.model.MenuItem;
import com.foodApp.model.Restaurant;
import com.foodApp.repository.MenuItemRepository;
import com.foodApp.repository.MenuItemRepositoryImp;
import com.foodApp.repository.RestaurantRepository;
import com.foodApp.repository.RestaurantRepositoryImp;
import com.foodApp.dto.MenuItemCreateUpdateDto;
import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.exception.UnauthorizedAccessException;
import com.foodApp.exception.MenuItemNotFoundException;
import com.foodApp.util.Message;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemServiceImpl implements ItemService {
    private final MenuItemRepository menuItemRepository = new MenuItemRepositoryImp();
    private final RestaurantRepository restaurantRepository = new RestaurantRepositoryImp();

    @Override
    public List<MenuItem> findItemsWithFilters(String search, BigDecimal maxPrice, List<String> keywords) {
        return menuItemRepository.findItemsWithFilters(search, maxPrice, keywords);
    }

    @Override
    public Optional<MenuItem> findItemById(Integer id) {
        return menuItemRepository.findById(id);
    }

    @Override
    public MenuItem createRestaurantMenuItem(Integer restaurantId, MenuItemCreateUpdateDto createDto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }
        // اعتبارسنجی مالکیت رستوران در هندلر انجام شده است.

        String keywordsString = (createDto.getKeywords() != null) ?
                String.join(",", createDto.getKeywords()) : null;

        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurant(restaurant);
        menuItem.setName(createDto.getName());
        menuItem.setImage(createDto.getImageBase64());
        menuItem.setDescription(createDto.getDescription());
        menuItem.setPrice(BigDecimal.valueOf(createDto.getPrice()));
        menuItem.setStock(createDto.getSupply());
        menuItem.setKeywords(keywordsString);
        menuItem.setCreatedAt(LocalDateTime.now());
        menuItem.setUpdatedAt(LocalDateTime.now());

        return menuItemRepository.save(menuItem);
    }

    @Override
    public MenuItem updateRestaurantMenuItem(Integer restaurantId, Integer menuItemId, MenuItemCreateUpdateDto updateDto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }
        MenuItem existingMenuItem = menuItemRepository.findById(menuItemId).orElse(null);
        if (existingMenuItem == null) {
            throw new MenuItemNotFoundException(Message.ERROR_404.get());
        }
        // اعتبارسنجی مالکیت: اطمینان از اینکه آیتم منو به رستوران مشخص شده تعلق دارد
        if (!existingMenuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedAccessException("Menu item does not belong to this restaurant.");
        }

        if (updateDto.getName() != null && !updateDto.getName().isBlank()) {
            existingMenuItem.setName(updateDto.getName());
        }
        if (updateDto.getImageBase64() != null) {
            existingMenuItem.setImage(updateDto.getImageBase64());
        }
        if (updateDto.getDescription() != null) {
            existingMenuItem.setDescription(updateDto.getDescription());
        }
        if (updateDto.getPrice() != null) {
            existingMenuItem.setPrice(BigDecimal.valueOf(updateDto.getPrice()));
        }
        if (updateDto.getSupply() != null) {
            existingMenuItem.setStock(updateDto.getSupply());
        }
        if (updateDto.getKeywords() != null) {
            String keywordsString = String.join(",", updateDto.getKeywords());
            existingMenuItem.setKeywords(keywordsString);
        }
        existingMenuItem.setUpdatedAt(LocalDateTime.now());

        return menuItemRepository.save(existingMenuItem);
    }

    @Override
    public void deleteRestaurantMenuItem(Integer restaurantId, Integer menuItemId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }
        MenuItem existingMenuItem = menuItemRepository.findById(menuItemId).orElse(null);
        if (existingMenuItem == null) {
            throw new MenuItemNotFoundException(Message.ERROR_404.get());
        }
        // اعتبارسنجی مالکیت
        if (!existingMenuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedAccessException("Menu item does not belong to this restaurant.");
        }

        menuItemRepository.delete(existingMenuItem);
    }
}