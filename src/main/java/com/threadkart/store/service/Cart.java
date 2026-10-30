package com.threadkart.store.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * One cart per browser session. Only ids, sizes and quantities live here -
 * prices are always read fresh from the database when the cart is shown.
 */
@Component
@SessionScope
public class Cart implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final int MAX_QTY = 10;

	private final Map<String, Line> lines = new LinkedHashMap<>();

	public void add(long productId, String size, int qty) {
		String key = key(productId, size);
		Line line = lines.get(key);
		if (line == null) {
			lines.put(key, new Line(productId, size, Math.min(qty, MAX_QTY)));
		} else {
			line.quantity = Math.min(line.quantity + qty, MAX_QTY);
		}
	}

	public void setQuantity(String key, int qty) {
		Line line = lines.get(key);
		if (line == null) {
			return;
		}
		if (qty <= 0) {
			lines.remove(key);
		} else {
			line.quantity = Math.min(qty, MAX_QTY);
		}
	}

	public void remove(String key) {
		lines.remove(key);
	}

	public void clear() {
		lines.clear();
	}

	public List<Line> getLines() {
		return new ArrayList<>(lines.values());
	}

	public int getCount() {
		return lines.values().stream().mapToInt(l -> l.quantity).sum();
	}

	public boolean isEmpty() {
		return lines.isEmpty();
	}

	static String key(long productId, String size) {
		return productId + ":" + (size == null ? "" : size);
	}

	public static class Line implements Serializable {
		private static final long serialVersionUID = 1L;
		private final long productId;
		private final String size;
		private int quantity;

		Line(long productId, String size, int quantity) {
			this.productId = productId;
			this.size = size;
			this.quantity = quantity;
		}

		public long getProductId() {
			return productId;
		}

		public String getSize() {
			return size;
		}

		public int getQuantity() {
			return quantity;
		}

		public String getKey() {
			return key(productId, size);
		}
	}
}
