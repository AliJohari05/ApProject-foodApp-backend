package com.foodApp.repository;

import com.foodApp.model.Category;
import com.foodApp.model.MenuItem; // ایمپورت جدید
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> findById(Integer id);
    List<Category> findAll();
    void delete(Integer id);
    Optional<Category> findByRestaurantIdAndTitle(Integer restaurantId, String title); // یافتن دسته بر اساس رستوران و عنوان
    Optional<Category> findByRestaurantIdAndMenuItemId(Integer restaurantId, Integer menuItemId); // برای بررسی وجود ارتباط آیتم در دسته (اختیاری، ممکن است از findByMenuItemId و فیلتر دستی استفاده شود)
    void addMenuItemToCategory(Category category, MenuItem menuItem); // جدید: لینک MenuItem به Category
    void removeMenuItemFromCategory(Category category, MenuItem menuItem); // جدید: حذف لینک MenuItem از Category
    List<Category> findCategoriesByRestaurantId(Integer restaurantId); // جدید: دریافت تمام دسته‌های یک رستوران
}