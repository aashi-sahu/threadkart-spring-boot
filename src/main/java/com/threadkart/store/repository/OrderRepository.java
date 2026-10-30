package com.threadkart.store.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.threadkart.store.model.Order;
import com.threadkart.store.model.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@EntityGraph(attributePaths = "items")
	Optional<Order> findByOrderNumber(String orderNumber);

	@EntityGraph(attributePaths = "items")
	List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);

	@EntityGraph(attributePaths = "items")
	List<Order> findAllByOrderByCreatedAtDesc();

	@EntityGraph(attributePaths = "items")
	List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

	@EntityGraph(attributePaths = "items")
	List<Order> findTop5ByOrderByCreatedAtDesc();

	long countByStatus(OrderStatus status);

	@Query("select coalesce(sum(o.total), 0) from CustomerOrder o where o.status <> com.threadkart.store.model.OrderStatus.CANCELLED")
	BigDecimal totalRevenue();
}
