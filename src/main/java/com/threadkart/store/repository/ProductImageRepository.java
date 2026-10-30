package com.threadkart.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.threadkart.store.model.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
