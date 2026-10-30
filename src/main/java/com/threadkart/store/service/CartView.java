package com.threadkart.store.service;

import java.math.BigDecimal;
import java.util.List;

import com.threadkart.store.model.Product;

public class CartView {

	private final List<Item> items;
	private final BigDecimal subtotal;
	private final BigDecimal shipping;
	private final BigDecimal freeShippingAbove;

	public CartView(List<Item> items, BigDecimal subtotal, BigDecimal shipping, BigDecimal freeShippingAbove) {
		this.items = items;
		this.subtotal = subtotal;
		this.shipping = shipping;
		this.freeShippingAbove = freeShippingAbove;
	}

	public List<Item> getItems() {
		return items;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public BigDecimal getShipping() {
		return shipping;
	}

	public BigDecimal getTotal() {
		return subtotal.add(shipping);
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	/** how much more to add to get free delivery (0 if already free) */
	public BigDecimal getAmountForFreeShipping() {
		BigDecimal diff = freeShippingAbove.subtract(subtotal);
		return diff.signum() > 0 ? diff : BigDecimal.ZERO;
	}

	public static class Item {
		private final String key;
		private final Product product;
		private final String size;
		private final int quantity;

		public Item(String key, Product product, String size, int quantity) {
			this.key = key;
			this.product = product;
			this.size = size;
			this.quantity = quantity;
		}

		public String getKey() {
			return key;
		}

		public Product getProduct() {
			return product;
		}

		public String getSize() {
			return size;
		}

		public int getQuantity() {
			return quantity;
		}

		public BigDecimal getLineTotal() {
			return product.getPrice().multiply(BigDecimal.valueOf(quantity));
		}
	}
}
