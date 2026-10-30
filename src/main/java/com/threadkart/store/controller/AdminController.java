package com.threadkart.store.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.threadkart.store.dto.ProductDTO;
import com.threadkart.store.model.Category;
import com.threadkart.store.model.OrderStatus;
import com.threadkart.store.service.CategoryService;
import com.threadkart.store.service.NotFoundException;
import com.threadkart.store.service.OrderService;
import com.threadkart.store.service.ProductService;
import com.threadkart.store.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final CategoryService categoryService;
	private final ProductService productService;
	private final OrderService orderService;
	private final UserService userService;

	public AdminController(CategoryService categoryService, ProductService productService,
			OrderService orderService, UserService userService) {
		this.categoryService = categoryService;
		this.productService = productService;
		this.orderService = orderService;
		this.userService = userService;
	}

	@GetMapping
	public String adminHome(Model model) {
		model.addAttribute("productCount", productService.getAllProduct().size());
		model.addAttribute("orderCount", orderService.countOrders());
		model.addAttribute("pendingCount", orderService.countByStatus(OrderStatus.PLACED));
		model.addAttribute("userCount", userService.countUsers());
		model.addAttribute("revenue", orderService.totalRevenue());
		model.addAttribute("recentOrders", orderService.getRecentOrders());
		model.addAttribute("lowStock", productService.getAllProduct().stream().filter(p -> p.getStock() < 5).toList());
		return "admin/adminHome";
	}

	// ---------- categories ----------

	@GetMapping("/categories")
	public String getCat(Model model) {
		model.addAttribute("categories", categoryService.getAllCategory());
		return "admin/categories";
	}

	@GetMapping("/categories/add")
	public String getCatAdd(Model model) {
		model.addAttribute("category", new Category());
		return "admin/categoriesAdd";
	}

	@GetMapping("/categories/update/{id}")
	public String updateCat(@PathVariable int id, Model model) {
		Category category = categoryService.getCategoryById(id)
				.orElseThrow(() -> new NotFoundException("Category not found"));
		model.addAttribute("category", category);
		return "admin/categoriesAdd";
	}

	@PostMapping("/categories/add")
	public String postCatAdd(@ModelAttribute("category") Category category, Model model, RedirectAttributes ra) {
		String error = categoryService.saveCategory(category);
		if (error != null) {
			model.addAttribute("error", error);
			return "admin/categoriesAdd";
		}
		ra.addFlashAttribute("success", "Category saved");
		return "redirect:/admin/categories";
	}

	@PostMapping("/categories/delete/{id}")
	public String deleteCat(@PathVariable int id, RedirectAttributes ra) {
		String error = categoryService.removeCategoryById(id);
		if (error != null) {
			ra.addFlashAttribute("error", error);
		} else {
			ra.addFlashAttribute("success", "Category deleted");
		}
		return "redirect:/admin/categories";
	}

	// ---------- products ----------

	@GetMapping("/products")
	public String products(Model model) {
		model.addAttribute("products", productService.getAllProduct());
		return "admin/products";
	}

	@GetMapping("/products/add")
	public String productAddGet(Model model) {
		model.addAttribute("productDTO", new ProductDTO());
		model.addAttribute("categories", categoryService.getAllCategory());
		return "admin/productsAdd";
	}

	@GetMapping("/products/update/{id}")
	public String updateProductGet(@PathVariable long id, Model model) {
		var product = productService.getProductById(id)
				.orElseThrow(() -> new NotFoundException("Product not found"));
		model.addAttribute("productDTO", productService.toDto(product));
		model.addAttribute("categories", categoryService.getAllCategory());
		return "admin/productsAdd";
	}

	@PostMapping("/products/add")
	public String productAddPost(@Valid @ModelAttribute("productDTO") ProductDTO productDTO, BindingResult result,
			@RequestParam(name = "productImage", required = false) MultipartFile file, Model model, RedirectAttributes ra) throws IOException {
		if (!productService.isAllowedImage(file)) {
			result.reject("image", "Only JPG, PNG or WEBP images are allowed");
		}
		if (result.hasErrors()) {
			model.addAttribute("categories", categoryService.getAllCategory());
			return "admin/productsAdd";
		}
		productService.saveProduct(productDTO, file);
		ra.addFlashAttribute("success", "Product saved");
		return "redirect:/admin/products";
	}

	@PostMapping("/products/delete/{id}")
	public String deleteProduct(@PathVariable long id, RedirectAttributes ra) {
		productService.removeProductById(id);
		ra.addFlashAttribute("success", "Product deleted");
		return "redirect:/admin/products";
	}

	// ---------- orders ----------

	@GetMapping("/orders")
	public String orders(@RequestParam(required = false) OrderStatus status, Model model) {
		model.addAttribute("orders", orderService.getAllOrders(status));
		model.addAttribute("statuses", OrderStatus.values());
		model.addAttribute("selectedStatus", status);
		return "admin/orders";
	}

	@GetMapping("/orders/{orderNumber}")
	public String orderDetail(@PathVariable String orderNumber, Model model) {
		model.addAttribute("order", orderService.getByNumber(orderNumber));
		model.addAttribute("statuses", OrderStatus.values());
		return "admin/orderDetail";
	}

	@PostMapping("/orders/{orderNumber}/status")
	public String updateStatus(@PathVariable String orderNumber, @RequestParam OrderStatus status,
			RedirectAttributes ra) {
		try {
			orderService.updateStatus(orderNumber, status);
			ra.addFlashAttribute("success", "Status changed to " + status);
		} catch (IllegalStateException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/admin/orders/" + orderNumber;
	}
}
