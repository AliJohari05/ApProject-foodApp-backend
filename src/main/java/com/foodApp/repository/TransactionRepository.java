package com.foodApp.repository;

import com.foodApp.model.TransactionModel;

import java.util.List;

public interface TransactionRepository {
    void save(TransactionModel transaction);
    List<TransactionModel> findByUserId(int userId);
}
