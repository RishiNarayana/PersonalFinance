package com.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.entity.Budget;
import com.entity.User;

public interface BudgetRepository extends MongoRepository<Budget, String> {
    List<Budget> findByUser(User user);
}
