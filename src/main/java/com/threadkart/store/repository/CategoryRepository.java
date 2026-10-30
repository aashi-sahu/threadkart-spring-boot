package com.threadkart.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.threadkart.store.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

	boolean existsByNameIgnoreCase(String name);

	java.util.Optional<Category> findByNameIgnoreCase(String name);
}
