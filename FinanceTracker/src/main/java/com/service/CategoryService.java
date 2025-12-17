package com.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entity.Category;
import com.entity.User;
import com.repository.CategoryRepository;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	public Category save(Category c) { return categoryRepository.save(c); }

	public List<Category> findByUser(User user) { return categoryRepository.findByUser(user); }
}
