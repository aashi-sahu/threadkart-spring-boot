package com.threadkart.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.threadkart.store.service.CartService;

@Controller
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping("/cart")
	public String cart(Model model) {
		model.addAttribute("cart", cartService.getCartView());
		return "shop/cart";
	}

	@PostMapping("/cart/add")
	public String addToCart(@RequestParam long productId,
			@RequestParam(required = false) String size,
			@RequestParam(defaultValue = "1") int qty,
			RedirectAttributes ra) {
		String error = cartService.addToCart(productId, size, qty);
		if (error != null) {
			ra.addFlashAttribute("error", error);
			return "redirect:/shop/product/" + productId;
		}
		ra.addFlashAttribute("success", "Added to your cart");
		return "redirect:/cart";
	}

	@PostMapping("/cart/update")
	public String update(@RequestParam String key, @RequestParam int qty) {
		cartService.updateQuantity(key, qty);
		return "redirect:/cart";
	}

	@PostMapping("/cart/remove")
	public String remove(@RequestParam String key, RedirectAttributes ra) {
		cartService.remove(key);
		ra.addFlashAttribute("success", "Item removed");
		return "redirect:/cart";
	}
}
