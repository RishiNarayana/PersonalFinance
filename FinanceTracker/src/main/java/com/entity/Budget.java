package com.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Budget entity supporting:
 * - Overall monthly budget (category = null)
 * - Category-wise budgets (category != null)
 * - Scoped by user + year + month
 * - Optional budget rollover
 */
@Document(collection = "budgets")
@CompoundIndex(name = "user_year_month_category_idx", 
                def = "{'user': 1, 'year': 1, 'month': 1, 'category': 1}", 
                unique = true)
public class Budget {

    @Id
    private String id;

    @DBRef
    private Category category; // null for overall budget, non-null for category budget

    private Double monthlyLimit; // Budget limit for the month

    @DBRef
    private User user;

    private Integer year; // Budget year (e.g., 2024)
    private Integer month; // Budget month (1-12)

    private Boolean allowRollover = false; // If true, unused budget rolls to next month
    private Boolean preventExceed = false; // If true, prevent expenses exceeding budget

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Double getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(Double monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Boolean getAllowRollover() { return allowRollover; }
    public void setAllowRollover(Boolean allowRollover) { this.allowRollover = allowRollover; }

    public Boolean getPreventExceed() { return preventExceed; }
    public void setPreventExceed(Boolean preventExceed) { this.preventExceed = preventExceed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Check if this is an overall budget (not category-specific)
     */
    public boolean isOverallBudget() {
        return category == null;
    }
}
