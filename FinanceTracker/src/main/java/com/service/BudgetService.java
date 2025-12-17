package com.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entity.Budget;
import com.entity.User;
import com.repository.BudgetRepository;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    public Budget save(Budget b) { return budgetRepository.save(b); }

    public List<Budget> findByUser(User user) { return budgetRepository.findByUser(user); }
}
