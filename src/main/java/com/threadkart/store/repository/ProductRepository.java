package com.threadkart.store.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.threadkart.store.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("""
			select p from Product p
			where (:categoryId is null or p.category.id = :categoryId)
			  and (:q is null or lower(p.name) like lower(concat('%', :q, '%'))
			       or lower(p.description) like lower(concat('%', :q, '%')))
			""")
	Page<Product> search(@Param("q") String q, @Param("categoryId") Integer categoryId, Pageable pageable);

	List<Product> findTop8ByOrderByCreatedAtDesc();

	List<Product> findTop4ByCategory_IdAndIdNot(Integer categoryId, Long id);

	long countByCategory_Id(Integer categoryId);

	long countByStockLessThan(int stock);
}
