package com.threadkart.store.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.threadkart.store.service.CartService;

/** Adds the cart badge count to every page so the navbar can show it. */
@ControllerAdvice
public class GlobalModelAttributes {

	private final CartService cartService;

	public GlobalModelAttributes(CartService cartService) {
		this.cartService = cartService;
	}

	@ModelAttribute("cartCount")
	public int cartCount() {
		return cartService.getCount();
	}
}
