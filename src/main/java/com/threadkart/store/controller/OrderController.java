package com.threadkart.store.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.threadkart.store.dto.CheckoutForm;
import com.threadkart.store.model.Order;
import com.threadkart.store.model.User;
import com.threadkart.store.service.CartService;
import com.threadkart.store.service.CartView;
import com.threadkart.store.service.NotFoundException;
import com.threadkart.store.service.OrderService;
import com.threadkart.store.service.UserService;

import jakarta.validation.Valid;

@Controller
public class OrderController {

	private final CartService cartService;
	private final OrderService orderService;
	private final UserService userService;

	public OrderController(CartService cartService, OrderService orderService, UserService userService) {
		this.cartService = cartService;
		this.orderService = orderService;
		this.userService = userService;
	}

	@GetMapping("/checkout")
	public String checkout(Model model, Principal principal) {
		CartView cart = cartService.getCartView();
		if (cart.isEmpty()) {
			return "redirect:/cart";
		}
		CheckoutForm form = new CheckoutForm();
		form.setFullName(userService.getByEmail(principal.getName()).getFullName());
		model.addAttribute("form", form);
		model.addAttribute("cart", cart);
		return "shop/checkout";
	}

	@PostMapping("/checkout")
	public String placeOrder(@Valid @ModelAttribute("form") CheckoutForm form, BindingResult result,
			Model model, Principal principal, RedirectAttributes ra) {
		CartView cart = cartService.getCartView();
		if (cart.isEmpty()) {
			return "redirect:/cart";
		}
		if (result.hasErrors()) {
			model.addAttribute("cart", cart);
			return "shop/checkout";
		}
		try {
			User user = userService.getByEmail(principal.getName());
			Order order = orderService.placeOrder(user, form, cart);
			cartService.clear();
			ra.addFlashAttribute("justPlaced", true);
			return "redirect:/orders/" + order.getOrderNumber();
		} catch (IllegalStateException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/cart";
		}
	}

	@GetMapping("/orders")
	public String myOrders(Model model, Principal principal) {
		User user = userService.getByEmail(principal.getName());
		model.addAttribute("orders", orderService.getOrdersForUser(user));
		return "account/orders";
	}

	@GetMapping("/orders/{orderNumber}")
	public String orderDetail(@PathVariable String orderNumber, Model model, Principal principal) {
		Order order = orderService.getByNumber(orderNumber);
		User user = userService.getByEmail(principal.getName());
		// customers should only see their own orders
		if (!order.getUser().getId().equals(user.getId())) {
			throw new NotFoundException("Order not found");
		}
		model.addAttribute("order", order);
		return "account/orderDetail";
	}

	@PostMapping("/orders/{orderNumber}/cancel")
	public String cancel(@PathVariable String orderNumber, Principal principal, RedirectAttributes ra) {
		try {
			orderService.cancelByCustomer(orderNumber, userService.getByEmail(principal.getName()));
			ra.addFlashAttribute("success", "Order cancelled");
		} catch (IllegalStateException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/orders/" + orderNumber;
	}
}
