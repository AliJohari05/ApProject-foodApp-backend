package com.foodApp.service;

import com.foodApp.dto.CreateMenuDto;
import com.foodApp.exception.RestaurantNotFoundException; // ایمپورت جدید
import com.foodApp.exception.MenuItemNotFoundException; // ایمپورت جدید
import com.foodApp.exception.UnauthorizedAccessException; // ایمپورت جدید
import com.foodApp.model.Category;
import com.foodApp.model.MenuItem;
import com.foodApp.model.Restaurant; // ایمپورت جدید
import com.foodApp.repository.CategoryRepository;
import com.foodApp.repository.CategoryRepositoryIml;
import com.foodApp.repository.MenuItemRepository; // ایمپورت جدید
import com.foodApp.repository.MenuItemRepositoryImp; // ایمپورت جدید
import com.foodApp.repository.RestaurantRepository; // ایمپورت جدید
import com.foodApp.repository.RestaurantRepositoryImp; // ایمپورت جدید
import com.foodApp.util.Message; // ایمپورت جدید

import java.time.LocalDateTime; // ایمپورت جدید
import java.util.List;
import java.util.Optional;

public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository = new CategoryRepositoryIml();
    private final RestaurantRepository restaurantRepository = new RestaurantRepositoryImp(); // تزریق
    private final MenuItemRepository menuItemRepository = new MenuItemRepositoryImp(); // تزریق

    @Override
    public void save(Category category) {
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    @Override
    public Optional<Category> findById(Integer id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public void delete(Integer id) {
        categoryRepository.delete(id);
    }

    @Override
    public Category createRestaurantCategory(Integer restaurantId, CreateMenuDto createDto) { // پیاده‌سازی متد جدید
        // اعتبارسنجی وجود رستوران
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }
        // بررسی اینکه عنوان دسته قبلاً برای این رستوران وجود نداشته باشد
        if (categoryRepository.findByRestaurantIdAndTitle(restaurantId, createDto.getTitle()).isPresent()) {
            throw new IllegalArgumentException(Message.CONFLICT.get()); // Conflict اگر عنوان تکراری باشد
        }

        Category category = new Category();
        category.setTitle(createDto.getTitle());
        category.setRestaurant(restaurant); // لینک به رستوران
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        categoryRepository.save(category);
        return category;
    }

    @Override
    public void deleteRestaurantCategory(Integer restaurantId, String categoryTitle) { // پیاده‌سازی متد جدید
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }
        Category category = categoryRepository.findByRestaurantIdAndTitle(restaurantId, categoryTitle).orElse(null);
        if (category == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get()); // دسته برای این رستوران یافت نشد
        }
        // اعتبارسنجی مالکیت از طریق findByRestaurantIdAndTitle انجام می‌شود
        categoryRepository.delete(category.getId());
    }

    @Override
    public void addMenuItemToCategory(Integer restaurantId, String categoryTitle, Integer menuItemId) { // پیاده‌سازی متد جدید
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }
        Category category = categoryRepository.findByRestaurantIdAndTitle(restaurantId, categoryTitle).orElse(null);
        if (category == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get()); // دسته برای این رستوران یافت نشد
        }
        MenuItem menuItem = menuItemRepository.findById(menuItemId).orElse(null);
        if (menuItem == null) {
            throw new MenuItemNotFoundException(Message.ERROR_404.get()); // آیتم منو یافت نشد
        }
        // بررسی حیاتی: اطمینان از اینکه MenuItem به همان رستورانی تعلق دارد که Category به آن تعلق دارد
        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedAccessException("Menu item does not belong to this restaurant.");
        }
        // بررسی اینکه آیتم از قبل در این دسته بندی نباشد
        if (category.getMenuItems().contains(menuItem)) {
            throw new IllegalArgumentException(Message.CONFLICT.get() + ": Menu item already exists in this category.");
        }

        categoryRepository.addMenuItemToCategory(category, menuItem);
    }

    @Override
    public void removeMenuItemFromCategory(Integer restaurantId, String categoryTitle, Integer menuItemId) { // پیاده‌سازی متد جدید
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }
        Category category = categoryRepository.findByRestaurantIdAndTitle(restaurantId, categoryTitle).orElse(null);
        if (category == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get()); // دسته برای این رستوران یافت نشد
        }
        MenuItem menuItem = menuItemRepository.findById(menuItemId).orElse(null);
        if (menuItem == null) {
            throw new MenuItemNotFoundException(Message.ERROR_404.get()); // آیتم منو یافت نشد
        }
        // بررسی حیاتی: اطمینان از اینکه MenuItem به همان رستورانی تعلق دارد که Category به آن تعلق دارد
        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedAccessException("Menu item does not belong to this restaurant.");
        }
        if (!category.getMenuItems().contains(menuItem)) {
            throw new IllegalArgumentException(Message.ERROR_404.get() + ": Menu item not found in this category.");
        }

        categoryRepository.removeMenuItemFromCategory(category, menuItem);
    }

    @Override
    public List<Category> getRestaurantCategories(Integer restaurantId) { // پیاده‌سازی متد جدید
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(Message.ERROR_404.get());
        }
        return categoryRepository.findCategoriesByRestaurantId(restaurantId);
    }

    @Override
    public Optional<Category> findRestaurantCategoryByTitle(Integer restaurantId, String categoryTitle) { // پیاده‌سازی متد جدید
        return categoryRepository.findByRestaurantIdAndTitle(restaurantId, categoryTitle);
    }


}