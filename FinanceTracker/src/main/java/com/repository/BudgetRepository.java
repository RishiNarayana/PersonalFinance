package com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.entity.Budget;
import com.entity.Category;
import com.entity.User;

public interface BudgetRepository extends MongoRepository<Budget, String> {
    List<Budget> findByUser(User user);
    
    // Find budget by user, year, month, and category (null for overall)
    Optional<Budget> findByUserAndYearAndMonthAndCategory(User user, Integer year, Integer month, Category category);
    
    // Find all budgets for a user in a specific month/year
    List<Budget> findByUserAndYearAndMonth(User user, Integer year, Integer month);
    
    // Find overall budget (category = null) for user/month/year
    Optional<Budget> findByUserAndYearAndMonthAndCategoryIsNull(User user, Integer year, Integer month);
    
    // Find category budgets for user/month/year
    List<Budget> findByUserAndYearAndMonthAndCategoryIsNotNull(User user, Integer year, Integer month);
}
