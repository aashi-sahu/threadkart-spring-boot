package com.threadkart.store.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.threadkart.store.model.Category;
import com.threadkart.store.repository.CategoryRepository;
import com.threadkart.store.repository.ProductRepository;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;

	public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
	}

	public List<Category> getAllCategory() {
		return categoryRepository.findAll(Sort.by("name"));
	}

	public Optional<Category> getCategoryById(int id) {
		return categoryRepository.findById(id);
	}

	/** returns an error message, or null when the category was saved */
	public String saveCategory(Category category) {
		String name = category.getName() == null ? "" : category.getName().trim();
		if (name.isEmpty()) {
			return "Category name can't be empty";
		}
		Optional<Category> sameName = categoryRepository.findByNameIgnoreCase(name);
		if (sameName.isPresent() && !sameName.get().getId().equals(category.getId())) {
			return "A category called '" + name + "' already exists";
		}
		category.setName(name);
		categoryRepository.save(category);
		return null;
	}

	/** returns an error message, or null when the category was deleted */
	public String removeCategoryById(int id) {
		long products = productRepository.countByCategory_Id(id);
		if (products > 0) {
			return "This category still has " + products + " product(s). Move or delete them first.";
		}
		categoryRepository.deleteById(id);
		return null;
	}
}
