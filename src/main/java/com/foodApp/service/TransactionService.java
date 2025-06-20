package com.foodApp.service;

import com.foodApp.model.TransactionModel;

import java.util.List;

public interface TransactionService {
    void save(TransactionModel transaction);
    List<TransactionModel> getByUserId(int userId);
    List<TransactionModel> findAll();
}
