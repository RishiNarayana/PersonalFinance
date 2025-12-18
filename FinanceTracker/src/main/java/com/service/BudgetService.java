package com.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dto.BudgetRequest;
import com.dto.BudgetStatusResponse;
import com.dto.MonthlySummaryResponse;
import com.entity.Budget;
import com.entity.Category;
import com.entity.Transaction;
import com.entity.User;
import com.repository.BudgetRepository;
import com.repository.CategoryRepository;
import com.repository.TransactionRepository;

/**
 * Comprehensive Budget Management Service
 * 
 * Features:
 * - Overall and category-wise budget management
 * - Real-time expense tracking
 * - Budget status calculation (SAFE, WARNING, EXCEEDED)
 * - Alert generation (50%, 75%, 90% thresholds)
 * - Budget rollover support
 * - Budget validation
 */
@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Alert thresholds
    private static final double ALERT_50 = 50.0;
    private static final double ALERT_75 = 75.0;
    private static final double ALERT_90 = 90.0;

    /**
     * Create or update a budget
     * Budgets are unique by user + year + month + category
     */
    @Transactional
    public Budget createOrUpdateBudget(User user, BudgetRequest request) {
        // Validate year and month
        Integer year = request.getYear();
        Integer month = request.getMonth();
        
        if (year == null || month == null) {
            // Default to current month if not specified
            YearMonth current = YearMonth.now();
            year = year != null ? year : current.getYear();
            month = month != null ? month : current.getMonthValue();
        }

        // Validate month range
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        // Validate budget limit
        if (request.getMonthlyLimit() == null || request.getMonthlyLimit() < 0) {
            throw new IllegalArgumentException("Monthly limit must be a positive number");
        }

        // Get category if specified
        Category category = null;
        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            
            // Verify category belongs to user
            if (!category.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Category does not belong to user");
            }
        }

        // Find existing budget
        Optional<Budget> existing = budgetRepository.findByUserAndYearAndMonthAndCategory(
                user, year, month, category);

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setUpdatedAt(java.time.LocalDateTime.now());
        } else {
            budget = new Budget();
            budget.setUser(user);
            budget.setYear(year);
            budget.setMonth(month);
            budget.setCategory(category);
        }

        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setAllowRollover(request.getAllowRollover() != null ? request.getAllowRollover() : false);
        budget.setPreventExceed(request.getPreventExceed() != null ? request.getPreventExceed() : false);

        return budgetRepository.save(budget);
    }

    /**
     * Get budget status for a specific month/year
     * Calculates real-time spending and status
     */
    public BudgetStatusResponse getBudgetStatus(User user, Integer year, Integer month) {
        if (year == null || month == null) {
            YearMonth current = YearMonth.now();
            year = current.getYear();
            month = current.getMonthValue();
        }

        // Get date range for the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get all budgets for the month
        List<Budget> budgets = budgetRepository.findByUserAndYearAndMonth(user, year, month);
        
        // Get overall budget
        Optional<Budget> overallBudgetOpt = budgetRepository.findByUserAndYearAndMonthAndCategoryIsNull(
                user, year, month);
        
        // Get all expenses AND income for the month
        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(
                user, startDate, endDate);

        double totalExpenses = transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .mapToDouble(tx -> tx.getAmount() != null ? tx.getAmount() : 0.0)
                .sum();
                
        double totalIncome = transactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                .mapToDouble(tx -> tx.getAmount() != null ? tx.getAmount() : 0.0)
                .sum();
        
        // Net spent = Expenses - Income (handling refunds/credits)
        double totalSpent = totalExpenses - totalIncome; 
        if (totalSpent < 0) totalSpent = 0.0; // Don't show negative spending even if income > expense

        // Build response
        BudgetStatusResponse response = new BudgetStatusResponse();
        List<BudgetStatusResponse.CategoryBudgetStatus> categoryStatuses = new ArrayList<>();
        List<BudgetStatusResponse.BudgetAlert> alerts = new ArrayList<>();

        // Process overall budget
        if (overallBudgetOpt.isPresent()) {
            Budget overallBudget = overallBudgetOpt.get();
            double budgetLimit = overallBudget.getMonthlyLimit();
            double remaining = budgetLimit - totalSpent;
            double usagePercentage = budgetLimit > 0 ? (totalSpent / budgetLimit) * 100 : 0;

            response.setOverallBudget(budgetLimit);
            response.setOverallSpent(totalSpent);
            response.setOverallRemaining(remaining);
            response.setOverallUsagePercentage(usagePercentage);
            response.setOverallStatus(calculateStatus(usagePercentage));

            // Generate alerts for overall budget
            alerts.addAll(generateAlerts("OVERALL", usagePercentage, budgetLimit, totalSpent));
        } else {
            response.setOverallBudget(0.0);
            response.setOverallSpent(totalSpent);
            response.setOverallRemaining(0.0);
            response.setOverallUsagePercentage(0.0);
            response.setOverallStatus(BudgetStatusResponse.BudgetStatus.SAFE);
        }

        // Process category budgets
        List<Budget> categoryBudgets = budgetRepository.findByUserAndYearAndMonthAndCategoryIsNotNull(
                user, year, month);

        for (Budget categoryBudget : categoryBudgets) {
            Category cat = categoryBudget.getCategory();
            double categoryLimit = categoryBudget.getMonthlyLimit();

            // Calculate category spending using DB query ensures reliable matching
            double categorySpent = getCurrentSpending(user, year, month, cat); 

            double categoryRemaining = categoryLimit - categorySpent;
            double categoryUsagePercentage = categoryLimit > 0 ? 
                    (categorySpent / categoryLimit) * 100 : 0;

            BudgetStatusResponse.CategoryBudgetStatus catStatus = 
                    new BudgetStatusResponse.CategoryBudgetStatus();
            catStatus.setCategoryId(cat.getId());
            catStatus.setCategoryName(cat.getName());
            catStatus.setBudget(categoryLimit);
            catStatus.setSpent(categorySpent);
            catStatus.setRemaining(categoryRemaining);
            catStatus.setUsagePercentage(categoryUsagePercentage);
            catStatus.setStatus(calculateStatus(categoryUsagePercentage));

            categoryStatuses.add(catStatus);

            // Generate alerts for category
            alerts.addAll(generateAlerts(cat.getId(), categoryUsagePercentage, 
                    categoryLimit, categorySpent));
        }

        response.setCategoryBudgets(categoryStatuses);
        response.setAlerts(alerts);

        return response;
    }

    /**
     * Calculate budget status based on usage percentage
     */
    private BudgetStatusResponse.BudgetStatus calculateStatus(double usagePercentage) {
        if (usagePercentage >= 90) {
            return BudgetStatusResponse.BudgetStatus.EXCEEDED;
        } else if (usagePercentage >= 50) {
            return BudgetStatusResponse.BudgetStatus.WARNING;
        } else {
            return BudgetStatusResponse.BudgetStatus.SAFE;
        }
    }

    /**
     * Generate alerts for budget thresholds
     */
    private List<BudgetStatusResponse.BudgetAlert> generateAlerts(
            String type, double usagePercentage, double budget, double spent) {
        
        List<BudgetStatusResponse.BudgetAlert> alerts = new ArrayList<>();

        // Check if we've crossed thresholds
        if (usagePercentage >= ALERT_90 && usagePercentage < 100) {
            BudgetStatusResponse.BudgetAlert alert = new BudgetStatusResponse.BudgetAlert();
            alert.setType(type);
            alert.setThreshold(ALERT_90);
            alert.setSeverity("CRITICAL");
            alert.setMessage(String.format("Budget is %.1f%% used (%.2f / %.2f). Approaching limit!",
                    usagePercentage, spent, budget));
            alerts.add(alert);
        } else if (usagePercentage >= ALERT_75 && usagePercentage < 90) {
            BudgetStatusResponse.BudgetAlert alert = new BudgetStatusResponse.BudgetAlert();
            alert.setType(type);
            alert.setThreshold(ALERT_75);
            alert.setSeverity("WARNING");
            alert.setMessage(String.format("Budget is %.1f%% used (%.2f / %.2f).",
                    usagePercentage, spent, budget));
            alerts.add(alert);
        } else if (usagePercentage >= ALERT_50 && usagePercentage < 75) {
            BudgetStatusResponse.BudgetAlert alert = new BudgetStatusResponse.BudgetAlert();
            alert.setType(type);
            alert.setThreshold(ALERT_50);
            alert.setSeverity("INFO");
            alert.setMessage(String.format("Budget is %.1f%% used (%.2f / %.2f).",
                    usagePercentage, spent, budget));
            alerts.add(alert);
        }

        // Exceeded budget alert
        if (usagePercentage >= 100) {
            BudgetStatusResponse.BudgetAlert alert = new BudgetStatusResponse.BudgetAlert();
            alert.setType(type);
            alert.setThreshold(100.0);
            alert.setSeverity("CRITICAL");
            alert.setMessage(String.format("Budget EXCEEDED! Spent %.2f exceeds limit of %.2f by %.2f",
                    spent, budget, spent - budget));
            alerts.add(alert);
        }

        return alerts;
    }

    /**
     * Validate if an expense can be added without exceeding budget
     * Returns validation result with warnings if applicable
     */
    public BudgetValidationResult validateExpense(User user, Transaction expense) {
        if (expense == null || expense.getType() == null || 
            !expense.getType().equals("EXPENSE")) {
            return new BudgetValidationResult(true, null, null);
        }

        LocalDate expenseDate = expense.getDate();
        if (expenseDate == null) {
            expenseDate = LocalDate.now();
        }

        YearMonth yearMonth = YearMonth.from(expenseDate);
        Integer year = yearMonth.getYear();
        Integer month = yearMonth.getMonthValue();

        double expenseAmount = expense.getAmount() != null ? expense.getAmount() : 0.0;

        // Check overall budget
        Optional<Budget> overallBudget = budgetRepository.findByUserAndYearAndMonthAndCategoryIsNull(
                user, year, month);

        if (overallBudget.isPresent()) {
            Budget budget = overallBudget.get();
            double currentSpent = getCurrentSpending(user, year, month, null);
            double newTotal = currentSpent + expenseAmount;

            if (newTotal > budget.getMonthlyLimit()) {
                if (budget.getPreventExceed()) {
                    return new BudgetValidationResult(false, 
                            "Expense would exceed overall monthly budget", 
                            BudgetStatusResponse.BudgetStatus.EXCEEDED);
                } else {
                    return new BudgetValidationResult(true, 
                            "Warning: Expense exceeds overall monthly budget", 
                            BudgetStatusResponse.BudgetStatus.EXCEEDED);
                }
            }
        }

        // Check category budget if category is specified
        if (expense.getCategory() != null) {
            Optional<Budget> categoryBudget = budgetRepository.findByUserAndYearAndMonthAndCategory(
                    user, year, month, expense.getCategory());

            if (categoryBudget.isPresent()) {
                Budget budget = categoryBudget.get();
                double currentCategorySpent = getCurrentSpending(user, year, month, expense.getCategory());
                double newCategoryTotal = currentCategorySpent + expenseAmount;

                if (newCategoryTotal > budget.getMonthlyLimit()) {
                    if (budget.getPreventExceed()) {
                        return new BudgetValidationResult(false, 
                                "Expense would exceed category budget: " + expense.getCategory().getName(), 
                                BudgetStatusResponse.BudgetStatus.EXCEEDED);
                    } else {
                        return new BudgetValidationResult(true, 
                                "Warning: Expense exceeds category budget: " + expense.getCategory().getName(), 
                                BudgetStatusResponse.BudgetStatus.EXCEEDED);
                    }
                }
            }
        }

        return new BudgetValidationResult(true, null, null);
    }

    /**
     * Get current spending for a user/month/category
     */
    private double getCurrentSpending(User user, Integer year, Integer month, Category category) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions; // Get all transactions to separate inc/exp
        if (category == null) {
            transactions = transactionRepository.findByUserAndDateBetween(
                    user, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserAndCategoryAndDateBetween(
                    user, category, startDate, endDate);
        }
        
        double expenses = transactions.stream()
                .filter(tx -> "EXPENSE".equalsIgnoreCase(tx.getType()))
                .mapToDouble(tx -> tx.getAmount() != null ? tx.getAmount() : 0.0)
                .sum();
                
        double income = transactions.stream()
                .filter(tx -> "INCOME".equalsIgnoreCase(tx.getType()))
                .mapToDouble(tx -> tx.getAmount() != null ? tx.getAmount() : 0.0)
                .sum();

        return Math.max(0, expenses - income);
    }

    /**
     * Get monthly summary (income, expenses, savings, budget status)
     */
    public MonthlySummaryResponse getMonthlySummary(User user, Integer year, Integer month) {
        if (year == null || month == null) {
            YearMonth current = YearMonth.now();
            year = current.getYear();
            month = current.getMonthValue();
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get all transactions for the month
        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(
                user, startDate, endDate);

        // Calculate income and expenses
        double totalIncome = transactions.stream()
                .filter(tx -> "INCOME".equals(tx.getType()))
                .mapToDouble(tx -> tx.getAmount() != null ? tx.getAmount() : 0.0)
                .sum();

        double totalExpenses = transactions.stream()
                .filter(tx -> "EXPENSE".equals(tx.getType()))
                .mapToDouble(tx -> tx.getAmount() != null ? tx.getAmount() : 0.0)
                .sum();

        double savings = totalIncome - totalExpenses;
        double savingsPercentage = totalIncome > 0 ? (savings / totalIncome) * 100 : 0;

        // Get budget status
        BudgetStatusResponse budgetStatus = getBudgetStatus(user, year, month);

        // Calculate category-wise expense breakdown
        List<MonthlySummaryResponse.CategoryExpense> categoryExpenses = transactions.stream()
                .filter(tx -> "EXPENSE".equals(tx.getType()) && tx.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(tx -> tx.getAmount() != null ? tx.getAmount() : 0.0)
                ))
                .entrySet().stream()
                .map(entry -> {
                    MonthlySummaryResponse.CategoryExpense catExp = 
                            new MonthlySummaryResponse.CategoryExpense();
                    catExp.setCategoryId(entry.getKey().getId());
                    catExp.setCategoryName(entry.getKey().getName());
                    catExp.setAmount(entry.getValue());
                    catExp.setPercentage(totalExpenses > 0 ? (entry.getValue() / totalExpenses) * 100.0 : 0.0);
                    return catExp;
                })
                .collect(Collectors.toList());

        MonthlySummaryResponse response = new MonthlySummaryResponse();
        response.setYear(year);
        response.setMonth(month);
        response.setTotalIncome(totalIncome);
        response.setTotalExpenses(totalExpenses);
        response.setSavings(savings);
        response.setSavingsPercentage(savingsPercentage);
        response.setBudgetStatus(budgetStatus);
        response.setCategoryExpenses(categoryExpenses);

        return response;
    }

    /**
     * Get all budgets for a user
     */
    public List<Budget> findByUser(User user) {
        return budgetRepository.findByUser(user);
    }

    /**
     * Get budgets for a specific month/year
     */
    public List<Budget> findByUserAndMonth(User user, Integer year, Integer month) {
        return budgetRepository.findByUserAndYearAndMonth(user, year, month);
    }

    /**
     * Delete a budget
     */
    @Transactional
    public void deleteBudget(String budgetId, User user) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized: Budget does not belong to user");
        }

        budgetRepository.delete(budget);
    }

    /**
     * Budget validation result
     */
    public static class BudgetValidationResult {
        private final boolean allowed;
        private final String message;
        private final BudgetStatusResponse.BudgetStatus status;

        public BudgetValidationResult(boolean allowed, String message, 
                BudgetStatusResponse.BudgetStatus status) {
            this.allowed = allowed;
            this.message = message;
            this.status = status;
        }

        public boolean isAllowed() { return allowed; }
        public String getMessage() { return message; }
        public BudgetStatusResponse.BudgetStatus getStatus() { return status; }
    }
}
