package com.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import com.repository.TransactionRepository;
import com.entity.Transaction;
import com.entity.User;
import com.service.TransactionService;
import com.service.BudgetService;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BudgetService budgetService;

    @PostMapping
    public Transaction add(@RequestBody Transaction transaction,
                           Authentication auth) {

        User user = (User) auth.getPrincipal();
        return transactionService.save(transaction, user);
    }

    @GetMapping
    public List<Transaction> getAll(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return transactionRepository.findByUser(user);
    }

    @PutMapping("/{id}")
    public Transaction update(@PathVariable String id,
                              @RequestBody Transaction updated,
                              Authentication auth) {

        User user = (User) auth.getPrincipal();
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Update fields
        tx.setAmount(updated.getAmount());
        tx.setDate(updated.getDate());
        tx.setNote(updated.getNote());
        tx.setCategory(updated.getCategory());
        tx.setType(updated.getType());

        // Validate budget if it's an expense
        if (tx.getType() != null && tx.getType().equals("EXPENSE")) {
            BudgetService.BudgetValidationResult validation = 
                    budgetService.validateExpense(user, tx);
            
            if (!validation.isAllowed()) {
                throw new IllegalArgumentException(validation.getMessage());
            }
        }

        return transactionRepository.save(tx);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Transaction tx = transactionRepository.findById(id).orElseThrow();

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        transactionRepository.delete(tx);
    }
}
