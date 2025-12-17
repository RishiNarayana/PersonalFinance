package com.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import com.repository.TransactionRepository;
import com.entity.Transaction;
import com.entity.User;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping
    public Transaction add(@RequestBody Transaction transaction,
                           Authentication auth) {

        User user = (User) auth.getPrincipal();
        transaction.setUser(user);
        return transactionRepository.save(transaction);
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
                .orElseThrow();

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        tx.setAmount(updated.getAmount());
        tx.setDate(updated.getDate());
        tx.setNote(updated.getNote());
        tx.setCategory(updated.getCategory());
        tx.setType(updated.getType());

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
