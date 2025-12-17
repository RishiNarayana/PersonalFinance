package com.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "budgets")
public class Budget {

    @Id
    private String id;

    @DBRef
    private Category category;

    private Double monthlyLimit;

    @DBRef
    private User user;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Double getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(Double monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
