package com.dto;

/**
 * Request DTO for creating/updating budgets
 */
public class BudgetRequest {
    private String categoryId; // null for overall budget
    private Double monthlyLimit;
    private Integer year;
    private Integer month;
    private Boolean allowRollover;
    private Boolean preventExceed;

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public Double getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(Double monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Boolean getAllowRollover() { return allowRollover; }
    public void setAllowRollover(Boolean allowRollover) { this.allowRollover = allowRollover; }

    public Boolean getPreventExceed() { return preventExceed; }
    public void setPreventExceed(Boolean preventExceed) { this.preventExceed = preventExceed; }
}

