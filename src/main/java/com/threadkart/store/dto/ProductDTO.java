package com.threadkart.store.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Backing object for the admin add / edit product form. */
public class ProductDTO {

	private Long id;

	@NotBlank(message = "Name is required")
	@Size(max = 120)
	private String name;

	@NotNull(message = "Pick a category")
	private Integer categoryId;

	@NotNull(message = "Price is required")
	@DecimalMin(value = "1", message = "Price should be at least ₹1")
	private BigDecimal price;

	@Min(value = 0, message = "Stock can't be negative")
	private int stock;

	@Size(max = 60)
	private String sizes;

	@Size(max = 1000, message = "Keep the description under 1000 characters")
	private String description;

	private Long imageId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public String getSizes() {
		return sizes;
	}

	public void setSizes(String sizes) {
		this.sizes = sizes;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getImageId() {
		return imageId;
	}

	public void setImageId(Long imageId) {
		this.imageId = imageId;
	}
}
