package com.dto;

import java.util.List;

/**
 * Response DTO for budget status including:
 * - Current usage
 * - Status (SAFE, WARNING, EXCEEDED)
 * - Alerts
 * - Category breakdowns
 */
public class BudgetStatusResponse {
    
    public enum BudgetStatus {
        SAFE,      // < 50% used
        WARNING,   // 50-90% used
        EXCEEDED   // > 90% used
    }

    private BudgetStatus overallStatus;
    private Double overallBudget;
    private Double overallSpent;
    private Double overallRemaining;
    private Double overallUsagePercentage;

    private List<CategoryBudgetStatus> categoryBudgets;
    private List<BudgetAlert> alerts;

    // Getters and Setters
    public BudgetStatus getOverallStatus() { return overallStatus; }
    public void setOverallStatus(BudgetStatus overallStatus) { this.overallStatus = overallStatus; }

    public Double getOverallBudget() { return overallBudget; }
    public void setOverallBudget(Double overallBudget) { this.overallBudget = overallBudget; }

    public Double getOverallSpent() { return overallSpent; }
    public void setOverallSpent(Double overallSpent) { this.overallSpent = overallSpent; }

    public Double getOverallRemaining() { return overallRemaining; }
    public void setOverallRemaining(Double overallRemaining) { this.overallRemaining = overallRemaining; }

    public Double getOverallUsagePercentage() { return overallUsagePercentage; }
    public void setOverallUsagePercentage(Double overallUsagePercentage) { this.overallUsagePercentage = overallUsagePercentage; }

    public List<CategoryBudgetStatus> getCategoryBudgets() { return categoryBudgets; }
    public void setCategoryBudgets(List<CategoryBudgetStatus> categoryBudgets) { this.categoryBudgets = categoryBudgets; }

    public List<BudgetAlert> getAlerts() { return alerts; }
    public void setAlerts(List<BudgetAlert> alerts) { this.alerts = alerts; }

    /**
     * Category-specific budget status
     */
    public static class CategoryBudgetStatus {
        private String categoryId;
        private String categoryName;
        private Double budget;
        private Double spent;
        private Double remaining;
        private Double usagePercentage;
        private BudgetStatus status;

        public String getCategoryId() { return categoryId; }
        public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public Double getBudget() { return budget; }
        public void setBudget(Double budget) { this.budget = budget; }

        public Double getSpent() { return spent; }
        public void setSpent(Double spent) { this.spent = spent; }

        public Double getRemaining() { return remaining; }
        public void setRemaining(Double remaining) { this.remaining = remaining; }

        public Double getUsagePercentage() { return usagePercentage; }
        public void setUsagePercentage(Double usagePercentage) { this.usagePercentage = usagePercentage; }

        public BudgetStatus getStatus() { return status; }
        public void setStatus(BudgetStatus status) { this.status = status; }
    }

    /**
     * Budget alert for threshold crossings
     */
    public static class BudgetAlert {
        private String type; // "OVERALL" or category ID
        private String message;
        private String severity; // "INFO", "WARNING", "CRITICAL"
        private Double threshold; // 50, 75, or 90

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public Double getThreshold() { return threshold; }
        public void setThreshold(Double threshold) { this.threshold = threshold; }
    }
}

