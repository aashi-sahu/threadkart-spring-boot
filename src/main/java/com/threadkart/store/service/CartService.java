package com.threadkart.store.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.threadkart.store.model.Product;
import com.threadkart.store.repository.ProductRepository;

@Service
public class CartService {

	private final Cart cart;
	private final ProductRepository productRepository;
	private final BigDecimal freeShippingAbove;
	private final BigDecimal shippingCharge;

	public CartService(Cart cart, ProductRepository productRepository,
			@Value("${app.shipping.free-above}") BigDecimal freeShippingAbove,
			@Value("${app.shipping.charge}") BigDecimal shippingCharge) {
		this.cart = cart;
		this.productRepository = productRepository;
		this.freeShippingAbove = freeShippingAbove;
		this.shippingCharge = shippingCharge;
	}

	/** returns an error message, or null when the item was added */
	public String addToCart(long productId, String size, int qty) {
		Optional<Product> found = productRepository.findById(productId);
		if (found.isEmpty()) {
			return "That product doesn't exist anymore";
		}
		Product product = found.get();
		if (!product.isInStock()) {
			return product.getName() + " is out of stock";
		}
		if (!product.getSizeList().isEmpty() && (size == null || !product.getSizeList().contains(size))) {
			return "Please choose a size";
		}
		if (product.getSizeList().isEmpty()) {
			size = null;
		}
		cart.add(productId, size, Math.max(qty, 1));
		return null;
	}

	public void updateQuantity(String key, int qty) {
		cart.setQuantity(key, qty);
	}

	public void remove(String key) {
		cart.remove(key);
	}

	public void clear() {
		cart.clear();
	}

	public int getCount() {
		return cart.getCount();
	}

	/**
	 * Builds what the cart page needs. Products deleted by the admin are dropped,
	 * and quantities are capped to what's actually in stock.
	 */
	public CartView getCartView() {
		List<CartView.Item> items = new ArrayList<>();
		BigDecimal subtotal = BigDecimal.ZERO;

		for (Cart.Line line : cart.getLines()) {
			Optional<Product> found = productRepository.findById(line.getProductId());
			if (found.isEmpty() || !found.get().isInStock()) {
				cart.remove(line.getKey());
				continue;
			}
			Product product = found.get();
			int qty = Math.min(line.getQuantity(), product.getStock());
			if (qty != line.getQuantity()) {
				cart.setQuantity(line.getKey(), qty);
			}
			CartView.Item item = new CartView.Item(line.getKey(), product, line.getSize(), qty);
			items.add(item);
			subtotal = subtotal.add(item.getLineTotal());
		}

		BigDecimal shipping = (items.isEmpty() || subtotal.compareTo(freeShippingAbove) >= 0)
				? BigDecimal.ZERO : shippingCharge;
		return new CartView(items, subtotal, shipping, freeShippingAbove);
	}
}
