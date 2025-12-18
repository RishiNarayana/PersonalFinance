package com.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dto.BudgetRequest;
import com.dto.BudgetStatusResponse;
import com.dto.MonthlySummaryResponse;
import com.entity.Budget;
import com.entity.User;
import com.service.BudgetService;

/**
 * REST API for Budget Management
 * 
 * Endpoints:
 * - POST /api/budgets - Create or update budget
 * - GET /api/budgets - List all budgets for user
 * - GET /api/budgets/status - Get budget status for month/year
 * - GET /api/budgets/monthly-summary - Get monthly financial summary
 * - DELETE /api/budgets/{id} - Delete budget
 */
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    /**
     * Create or update a budget
     * If budget exists for user+year+month+category, it will be updated
     */
    @PostMapping
    public ResponseEntity<Budget> createOrUpdate(@RequestBody BudgetRequest request, 
                                                  Authentication auth) {
        User user = (User) auth.getPrincipal();
        Budget budget = budgetService.createOrUpdateBudget(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    /**
     * Update an existing budget
     */
    @PutMapping("/{id}")
    public ResponseEntity<Budget> update(@PathVariable String id,
                                        @RequestBody BudgetRequest request,
                                        Authentication auth) {
        User user = (User) auth.getPrincipal();
        Budget budget = budgetService.createOrUpdateBudget(user, request);
        return ResponseEntity.ok(budget);
    }

    /**
     * Get all budgets for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<Budget>> list(Authentication auth) {
        User user = (User) auth.getPrincipal();
        List<Budget> budgets = budgetService.findByUser(user);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Get budgets for a specific month/year
     * Query params: year, month (optional - defaults to current month)
     */
    @GetMapping("/month")
    public ResponseEntity<List<Budget>> getBudgetsForMonth(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        List<Budget> budgets = budgetService.findByUserAndMonth(user, year, month);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Get budget status for a specific month/year
     * Includes real-time spending, status, alerts, and category breakdowns
     * Query params: year, month (optional - defaults to current month)
     */
    @GetMapping("/status")
    public ResponseEntity<BudgetStatusResponse> getBudgetStatus(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        BudgetStatusResponse status = budgetService.getBudgetStatus(user, year, month);
        return ResponseEntity.ok(status);
    }

    /**
     * Get monthly financial summary
     * Includes income, expenses, savings, budget status, and category breakdown
     * Query params: year, month (optional - defaults to current month)
     */
    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        MonthlySummaryResponse summary = budgetService.getMonthlySummary(user, year, month);
        return ResponseEntity.ok(summary);
    }

    /**
     * Delete a budget
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        budgetService.deleteBudget(id, user);
        return ResponseEntity.noContent().build();
    }
}
