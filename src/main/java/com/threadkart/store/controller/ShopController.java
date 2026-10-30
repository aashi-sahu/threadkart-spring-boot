package com.threadkart.store.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.threadkart.store.model.Product;
import com.threadkart.store.model.ProductImage;
import com.threadkart.store.service.CategoryService;
import com.threadkart.store.service.NotFoundException;
import com.threadkart.store.service.ProductService;

@Controller
public class ShopController {

	private final ProductService productService;
	private final CategoryService categoryService;

	public ShopController(ProductService productService, CategoryService categoryService) {
		this.productService = productService;
		this.categoryService = categoryService;
	}

	@GetMapping("/shop")
	public String shop(@RequestParam(required = false) String q,
			@RequestParam(required = false) Integer category,
			@RequestParam(required = false, defaultValue = "new") String sort,
			@RequestParam(required = false, defaultValue = "0") int page,
			Model model) {
		Page<Product> products = productService.search(q, category, sort, page);
		model.addAttribute("products", products);
		model.addAttribute("categories", categoryService.getAllCategory());
		model.addAttribute("q", q);
		model.addAttribute("selectedCategory", category);
		model.addAttribute("sort", sort);
		return "shop/shop";
	}

	@GetMapping("/shop/product/{id}")
	public String viewProduct(@PathVariable long id, Model model) {
		Product product = productService.getProductById(id)
				.orElseThrow(() -> new NotFoundException("Product not found"));
		model.addAttribute("product", product);
		model.addAttribute("related", productService.getRelatedProducts(product));
		return "shop/viewProduct";
	}

	@GetMapping("/product-images/{id}")
	public ResponseEntity<byte[]> image(@PathVariable long id) {
		ProductImage image = productService.getImage(id)
				.orElseThrow(() -> new NotFoundException("Image not found"));
		MediaType type = image.getContentType() == null ? MediaType.IMAGE_PNG
				: MediaType.parseMediaType(image.getContentType());
		// a new upload always gets a new id, so the browser can cache these for a long time
		return ResponseEntity.ok()
				.contentType(type)
				.cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
				.body(image.getData());
	}
}
