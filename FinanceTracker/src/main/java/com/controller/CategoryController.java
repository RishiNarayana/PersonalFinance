package com.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import com.repository.CategoryRepository;
import com.entity.Category;
import com.entity.User;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping
    public Category create(@RequestBody Category category,
                           Authentication auth) {

        User user = (User) auth.getPrincipal();
        category.setUser(user);
        return categoryRepository.save(category);
    }

    @GetMapping
    public List<Category> getAll(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return categoryRepository.findByUser(user);
    }
}
