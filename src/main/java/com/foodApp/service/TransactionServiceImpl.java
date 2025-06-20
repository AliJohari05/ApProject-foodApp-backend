package com.foodApp.service;

import com.foodApp.model.TransactionModel;
import com.foodApp.repository.TransactionRepository;
import com.foodApp.repository.TransactionRepositoryImpl;

import java.util.List;

public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repo = new TransactionRepositoryImpl();

    @Override
    public void save(TransactionModel transaction) {
        repo.save(transaction);
    }

    @Override
    public List<TransactionModel> getByUserId(int userId) {
        return repo.findByUserId(userId);
    }

    @Override
    public List<TransactionModel> findAll() {
        return repo.findAll();
    }
}
