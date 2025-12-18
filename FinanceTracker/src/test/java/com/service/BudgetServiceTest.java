package com.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dto.BudgetStatusResponse;
import com.entity.Budget;
import com.entity.Category;
import com.entity.Transaction;
import com.entity.User;
import com.repository.BudgetRepository;
import com.repository.CategoryRepository;
import com.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {

    @InjectMocks
    private BudgetService budgetService;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    public void testGetBudgetStatus_NetSpendCalculation() {
        User user = new User();
        user.setId("user1");

        // Mock Budget
        Budget budget = new Budget();
        budget.setMonthlyLimit(200.0);
        when(budgetRepository.findByUserAndYearAndMonthAndCategoryIsNull(any(), any(), any()))
                .thenReturn(Optional.of(budget));

        // Mock Transactions (Expense and Income)
        Transaction expense = new Transaction();
        expense.setType("EXPENSE");
        expense.setAmount(100.0);
        
        Transaction income = new Transaction();
        income.setType("INCOME"); // Refund
        income.setAmount(20.0);

        when(transactionRepository.findByUserAndDateBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(expense, income));

        // Execute
        BudgetStatusResponse response = budgetService.getBudgetStatus(user, 2024, 5);

        // Verify: Net Spend should be 100 - 20 = 80
        assertEquals(80.0, response.getOverallSpent());
        assertEquals(120.0, response.getOverallRemaining());
    }

    @Test
    public void testGetBudgetStatus_CategoryNetSpend() {
        User user = new User();
        user.setId("user1");
        
        Category cat = new Category();
        cat.setId("cat1");
        cat.setName("Food");

        // Mock Category Budget
        Budget catBudget = new Budget();
        catBudget.setCategory(cat);
        catBudget.setMonthlyLimit(100.0);
        
        when(budgetRepository.findByUserAndYearAndMonthAndCategoryIsNotNull(any(), any(), any()))
                .thenReturn(Arrays.asList(catBudget));

        // Mock Transactions
        Transaction expense = new Transaction();
        expense.setType("EXPENSE");
        expense.setAmount(50.0);
        expense.setCategory(cat);
        
        Transaction income = new Transaction();
        income.setType("INCOME");
        income.setAmount(10.0);
        income.setCategory(cat);

        when(transactionRepository.findByUserAndDateBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(expense, income));

        // Execute
        BudgetStatusResponse response = budgetService.getBudgetStatus(user, 2024, 5);

        // Verify Category: Net Spend = 50 - 10 = 40
        BudgetStatusResponse.CategoryBudgetStatus status = response.getCategoryBudgets().get(0);
        assertEquals(40.0, status.getSpent());
        assertEquals(60.0, status.getRemaining());
    }
}
