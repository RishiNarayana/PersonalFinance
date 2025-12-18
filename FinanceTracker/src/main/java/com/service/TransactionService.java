package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.entity.Transaction;
import com.entity.User;
import com.repository.TransactionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private BudgetService budgetService;

	/**
	 * Save a transaction with budget validation
	 * If budget validation fails and preventExceed is true, throws exception
	 */
	@Transactional
	public Transaction save(Transaction tx, User user) {
		// Validate budget before saving expense
		if (tx.getType() != null && tx.getType().equals("EXPENSE")) {
			BudgetService.BudgetValidationResult validation = 
					budgetService.validateExpense(user, tx);
			
			if (!validation.isAllowed()) {
				throw new IllegalArgumentException(validation.getMessage());
			}
			// If allowed but with warning, transaction still proceeds
			// Frontend can check for warnings if needed
		}
		
		tx.setUser(user);
		return transactionRepository.save(tx);
	}

	public Transaction save(Transaction tx) { 
		return transactionRepository.save(tx); 
	}

	public List<Transaction> findByUser(User user) { 
		return transactionRepository.findByUser(user); 
	}

	public Optional<Transaction> findById(String id) { 
		return transactionRepository.findById(id); 
	}

	public void delete(Transaction tx) { 
		transactionRepository.delete(tx); 
	}
}
