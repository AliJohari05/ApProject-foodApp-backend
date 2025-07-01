package com.foodApp.service;

import com.foodApp.dto.CreateMenuDto;
import com.foodApp.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    void save(Category category);
    Optional<Category> findById(Integer id);
    List<Category> findAll();
    void delete(Integer id);

    Category createRestaurantCategory(Integer restaurantId, CreateMenuDto createDto); // نامگذاری برای شفافیت
    void deleteRestaurantCategory(Integer restaurantId, String categoryTitle); // نامگذاری
    void addMenuItemToCategory(Integer restaurantId, String categoryTitle, Integer menuItemId);
    void removeMenuItemFromCategory(Integer restaurantId, String categoryTitle, Integer menuItemId);
    List<Category> getRestaurantCategories(Integer restaurantId); // جدید: دریافت تمام دسته‌های یک رستوران
    Optional<Category> findRestaurantCategoryByTitle(Integer restaurantId, String categoryTitle); // جدید: یافتن دسته خاص با عنوان برای یک رستوران
}