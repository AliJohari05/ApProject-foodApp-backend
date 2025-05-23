package com.foodApp.service;

import com.foodApp.exception.DuplicatePhoneException;
import com.foodApp.exception.InvalidPasswordException;
import com.foodApp.exception.UserNotFoundException;
import com.foodApp.model.Role;
import com.foodApp.model.User;
import com.foodApp.repository.UserRepository;
import com.foodApp.repository.UserRepositoryImp;
import java.util.List;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepo = new UserRepositoryImp();
    @Override
    public void registerUser(User user) {
        User checkPhoneUser = userRepo.findByPhone(user.getPhone());
        if (checkPhoneUser != null) {
            throw new DuplicatePhoneException("Phone number already in use");
        }
        userRepo.save(user);
    }
    @Override
    public User findById(int id) {
        return userRepo.findById(id);
    }

    @Override
    public User findByPhone(String phone) {
        return userRepo.findByPhone(phone);
    }

    @Override
    public User login(String phone, String password) {
        User user = userRepo.findByPhone(phone);
        if(user == null) {
            throw new UserNotFoundException("User not found");
        }
        if(!user.getPassword().equals(password)) {
            throw new InvalidPasswordException("Invalid password");
        }
        return user;
    }
    public void updateUser(User user) {
        User existingUser = userRepo.findById(user.getUserId());
        if(existingUser != null) {
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setAddress(user.getAddress());
            existingUser.setProfileImageUrl(user.getProfileImageUrl());
            existingUser.setBankName(user.getBankName());
            existingUser.setAccountNumber(user.getAccountNumber());

            userRepo.save(existingUser);
        } else {
            throw new UserNotFoundException("User not found");
        }
    }
    @Override
    public List<User> findAllByRole(Role role) {
        return userRepo.findByRole(role);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public void delete(int id) {
        userRepo.deleteById(id);

    }

    @Override
    public boolean phoneExists(String phone) {
        User user = userRepo.findByPhone(phone);
        if(user == null) return false;
        return true;

    }
}
