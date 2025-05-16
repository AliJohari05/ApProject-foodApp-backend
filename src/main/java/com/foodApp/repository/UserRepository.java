package com.foodApp.repository;

import com.foodApp.model.User;

import java.util.List;

public interface UserRepository {
    void save(User user);
    User findById(int id);
    User findByPhone(String phone);
    User findByRole(String role);
    List<User> findAll();
    void deleteById(int id);
}
