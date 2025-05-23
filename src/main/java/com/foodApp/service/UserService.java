package com.foodApp.service;

import com.foodApp.model.User;
import com.foodApp.model.Role;

import java.util.List;

public interface UserService {
    void registerUser(User user);
    User findById(int id);
    User findByPhone(String phone);
    User login(String phone, String password);
    void updateUser(User user);
    List<User> findAllByRole(Role role);
    List<User> findAllUsers();
    void delete(int id);
    boolean phoneExists(String phone);
}
