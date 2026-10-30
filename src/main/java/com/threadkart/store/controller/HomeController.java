package com.threadkart.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.threadkart.store.service.CategoryService;
import com.threadkart.store.service.ProductService;

@Controller
public class HomeController {

	private final ProductService productService;
	private final CategoryService categoryService;

	public HomeController(ProductService productService, CategoryService categoryService) {
		this.productService = productService;
		this.categoryService = categoryService;
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("products", productService.getLatestProducts());
		model.addAttribute("categories", categoryService.getAllCategory());
		return "index";
	}
}
