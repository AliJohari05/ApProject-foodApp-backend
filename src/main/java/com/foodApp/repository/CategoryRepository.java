package com.foodApp.repository;

import com.foodApp.model.Category;

import java.util.List;

public interface CategoryRepository {
    void save(Category category);
    Category findById(String id);
    List<Category> findAll();
    void delete(String id);

}
