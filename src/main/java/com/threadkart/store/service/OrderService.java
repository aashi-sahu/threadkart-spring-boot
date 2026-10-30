package com.threadkart.store.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.threadkart.store.dto.CheckoutForm;
import com.threadkart.store.model.Order;
import com.threadkart.store.model.OrderItem;
import com.threadkart.store.model.OrderStatus;
import com.threadkart.store.model.Product;
import com.threadkart.store.model.User;
import com.threadkart.store.repository.OrderRepository;
import com.threadkart.store.repository.ProductRepository;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;

	public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
	}

	@Transactional
	public Order placeOrder(User user, CheckoutForm form, CartView cart) {
		if (cart.isEmpty()) {
			throw new IllegalStateException("Your cart is empty");
		}

		Order order = new Order();
		order.setOrderNumber(newOrderNumber());
		order.setUser(user);
		order.setFullName(form.getFullName().trim());
		order.setPhone(form.getPhone());
		order.setAddressLine1(form.getAddressLine1().trim());
		order.setAddressLine2(form.getAddressLine2());
		order.setCity(form.getCity().trim());
		order.setState(form.getState().trim());
		order.setPincode(form.getPincode());

		for (CartView.Item cartItem : cart.getItems()) {
			// re-read the product inside the transaction so the stock check is up to date
			Product product = productRepository.findById(cartItem.getProduct().getId())
					.orElseThrow(() -> new IllegalStateException("A product in your cart was removed"));
			if (product.getStock() < cartItem.getQuantity()) {
				throw new IllegalStateException("Only " + product.getStock() + " left of " + product.getName());
			}
			product.setStock(product.getStock() - cartItem.getQuantity());

			OrderItem item = new OrderItem();
			item.setProductId(product.getId());
			item.setImageId(product.getImageId());
			item.setProductName(product.getName());
			item.setSize(cartItem.getSize());
			item.setUnitPrice(product.getPrice());
			item.setQuantity(cartItem.getQuantity());
			order.addItem(item);
		}

		order.setSubtotal(cart.getSubtotal());
		order.setShippingCharge(cart.getShipping());
		order.setTotal(cart.getTotal());
		return orderRepository.save(order);
	}

	public List<Order> getOrdersForUser(User user) {
		return orderRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
	}

	public Order getByNumber(String orderNumber) {
		return orderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> new NotFoundException("Order not found"));
	}

	public List<Order> getAllOrders(OrderStatus status) {
		return status == null ? orderRepository.findAllByOrderByCreatedAtDesc()
				: orderRepository.findByStatusOrderByCreatedAtDesc(status);
	}

	public List<Order> getRecentOrders() {
		return orderRepository.findTop5ByOrderByCreatedAtDesc();
	}

	@Transactional
	public void cancelByCustomer(String orderNumber, User user) {
		Order order = getByNumber(orderNumber);
		if (!order.getUser().getId().equals(user.getId())) {
			throw new NotFoundException("Order not found");
		}
		if (order.getStatus() != OrderStatus.PLACED) {
			throw new IllegalStateException("Only orders that haven't shipped yet can be cancelled");
		}
		changeStatus(order, OrderStatus.CANCELLED);
	}

	@Transactional
	public void updateStatus(String orderNumber, OrderStatus status) {
		changeStatus(getByNumber(orderNumber), status);
	}

	private void changeStatus(Order order, OrderStatus newStatus) {
		if (order.getStatus() == newStatus) {
			return;
		}
		if (order.getStatus() == OrderStatus.CANCELLED) {
			throw new IllegalStateException("A cancelled order can't be reopened");
		}
		// put the items back on the shelf
		if (newStatus == OrderStatus.CANCELLED) {
			for (OrderItem item : order.getItems()) {
				productRepository.findById(item.getProductId())
						.ifPresent(p -> p.setStock(p.getStock() + item.getQuantity()));
			}
		}
		order.setStatus(newStatus);
	}

	public long countByStatus(OrderStatus status) {
		return orderRepository.countByStatus(status);
	}

	public long countOrders() {
		return orderRepository.count();
	}

	public BigDecimal totalRevenue() {
		return orderRepository.totalRevenue();
	}

	// e.g. TK-301026-4821
	private String newOrderNumber() {
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy"));
		String number;
		do {
			number = "TK-" + date + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
		} while (orderRepository.findByOrderNumber(number).isPresent());
		return number;
	}
}
