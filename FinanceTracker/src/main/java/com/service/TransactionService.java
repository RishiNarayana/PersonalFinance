package com.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.repository.TransactionRepository;
import com.entity.Transaction;
import com.entity.User;
import java.util.List;

@Service
public class TransactionService {

	@Autowired
	private TransactionRepository transactionRepository;

	public Transaction save(Transaction tx) { return transactionRepository.save(tx); }

	public List<Transaction> findByUser(User user) { return transactionRepository.findByUser(user); }

	public java.util.Optional<Transaction> findById(String id) { return transactionRepository.findById(id); }

	public void delete(Transaction tx) { transactionRepository.delete(tx); }
}
