package com.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.entity.Transaction;
import com.entity.User;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUser(User user);
}
