package com.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.entity.Category;
import com.entity.Transaction;
import com.entity.User;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUser(User user);
    
    // Find expenses (type = "EXPENSE") for a user in a specific month/year
    List<Transaction> findByUserAndTypeAndDateBetween(
        User user, 
        String type, 
        LocalDate startDate, 
        LocalDate endDate
    );
    
    // Find expenses for a specific category in a date range
    List<Transaction> findByUserAndTypeAndCategoryAndDateBetween(
        User user,
        String type,
        Category category,
        LocalDate startDate,
        LocalDate endDate
    );
    
    // Find all transactions (income + expenses) for a user in a date range
    List<Transaction> findByUserAndDateBetween(
        User user,
        LocalDate startDate,
        LocalDate endDate
    );

    // Find all transactions for a specific category in a date range
    List<Transaction> findByUserAndCategoryAndDateBetween(
        User user,
        Category category,
        LocalDate startDate,
        LocalDate endDate
    );
}
