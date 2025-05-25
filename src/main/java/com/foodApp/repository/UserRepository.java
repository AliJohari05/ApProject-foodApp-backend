package com.foodApp.repository;

import com.foodApp.model.Role;
import com.foodApp.model.User;

import java.util.List;

public interface UserRepository {
    User save(User user);
    User findById(int id);
    User findByPhone(String phone);
    List<User> findByRole(Role role);
    List<User> findAll();
    void deleteById(int id);
}
