package com.dto;

import java.util.List;

/**
 * Monthly financial summary including income, expenses, savings, and budget status
 */
public class MonthlySummaryResponse {
    private Integer year;
    private Integer month;
    private Double totalIncome;
    private Double totalExpenses;
    private Double savings; // income - expenses
    private Double savingsPercentage; // (savings / income) * 100
    private BudgetStatusResponse budgetStatus;

    // Category-wise expense breakdown
    private List<CategoryExpense> categoryExpenses;

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(Double totalIncome) { this.totalIncome = totalIncome; }

    public Double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(Double totalExpenses) { this.totalExpenses = totalExpenses; }

    public Double getSavings() { return savings; }
    public void setSavings(Double savings) { this.savings = savings; }

    public Double getSavingsPercentage() { return savingsPercentage; }
    public void setSavingsPercentage(Double savingsPercentage) { this.savingsPercentage = savingsPercentage; }

    public BudgetStatusResponse getBudgetStatus() { return budgetStatus; }
    public void setBudgetStatus(BudgetStatusResponse budgetStatus) { this.budgetStatus = budgetStatus; }

    public List<CategoryExpense> getCategoryExpenses() { return categoryExpenses; }
    public void setCategoryExpenses(List<CategoryExpense> categoryExpenses) { this.categoryExpenses = categoryExpenses; }

    public static class CategoryExpense {
        private String categoryId;
        private String categoryName;
        private Double amount;
        private Double percentage; // of total expenses

        public String getCategoryId() { return categoryId; }
        public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }
}

