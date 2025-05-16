package com.foodApp.service;

import com.foodApp.model.User;
import com.foodApp.model.Role;

import java.util.List;

public interface UserService {
    void registerUser(User user);
    User findById(int id);
    User login(String phone, String password);
    List<User> findAllByRole(Role role);
    void delete(int id);
    
}
