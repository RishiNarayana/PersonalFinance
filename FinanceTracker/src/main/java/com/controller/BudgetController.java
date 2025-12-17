package com.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entity.Budget;
import com.entity.User;
import com.repository.BudgetRepository;
import com.service.BudgetService;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BudgetRepository budgetRepository;

    @PostMapping
    public Budget create(@RequestBody Budget budget, Authentication auth) {
        User user = (User) auth.getPrincipal();
        budget.setUser(user);
        return budgetService.save(budget);
    }

    @GetMapping
    public List<Budget> list(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return budgetService.findByUser(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        budgetRepository.delete(budget);
    }
}
