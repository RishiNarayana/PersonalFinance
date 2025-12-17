package com.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.entity.Category;
import com.entity.User;

public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByUser(User user);
}
