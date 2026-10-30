package com.threadkart.store.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.threadkart.store.dto.ProductDTO;
import com.threadkart.store.model.Category;
import com.threadkart.store.model.Product;
import com.threadkart.store.model.ProductImage;
import com.threadkart.store.repository.CategoryRepository;
import com.threadkart.store.repository.ProductImageRepository;
import com.threadkart.store.repository.ProductRepository;

@Service
public class ProductService {

	public static final int PAGE_SIZE = 9;
	private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

	private final ProductRepository productRepository;
	private final ProductImageRepository imageRepository;
	private final CategoryRepository categoryRepository;

	public ProductService(ProductRepository productRepository, ProductImageRepository imageRepository,
			CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
		this.categoryRepository = categoryRepository;
	}

	public List<Product> getAllProduct() {
		return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
	}

	public Optional<Product> getProductById(long id) {
		return productRepository.findById(id);
	}

	public List<Product> getLatestProducts() {
		return productRepository.findTop8ByOrderByCreatedAtDesc();
	}

	public List<Product> getRelatedProducts(Product product) {
		return productRepository.findTop4ByCategory_IdAndIdNot(product.getCategory().getId(), product.getId());
	}

	public Page<Product> search(String q, Integer categoryId, String sort, int page) {
		Sort order = switch (sort == null ? "" : sort) {
			case "price_asc" -> Sort.by("price").ascending();
			case "price_desc" -> Sort.by("price").descending();
			default -> Sort.by("createdAt").descending();
		};
		String query = (q == null || q.isBlank()) ? null : q.trim();
		return productRepository.search(query, categoryId, PageRequest.of(Math.max(page, 0), PAGE_SIZE, order));
	}

	@Transactional
	public Product saveProduct(ProductDTO dto, MultipartFile file) throws IOException {
		Product product = dto.getId() == null ? new Product()
				: productRepository.findById(dto.getId()).orElseThrow(() -> new NotFoundException("Product not found"));

		Category category = categoryRepository.findById(dto.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("Category not found"));

		product.setName(dto.getName().trim());
		product.setCategory(category);
		product.setPrice(dto.getPrice());
		product.setStock(dto.getStock());
		product.setSizes(cleanSizes(dto.getSizes()));
		product.setDescription(dto.getDescription());

		// old images are not deleted on purpose - past orders still show them
		if (file != null && !file.isEmpty()) {
			ProductImage image = imageRepository.save(new ProductImage(file.getBytes(), file.getContentType()));
			product.setImageId(image.getId());
		}
		return productRepository.save(product);
	}

	public boolean isAllowedImage(MultipartFile file) {
		return file == null || file.isEmpty() || ALLOWED_TYPES.contains(file.getContentType());
	}

	public void removeProductById(long id) {
		// the image row stays so order history keeps its thumbnails
		productRepository.deleteById(id);
	}

	public Optional<ProductImage> getImage(long imageId) {
		return imageRepository.findById(imageId);
	}

	public ProductDTO toDto(Product product) {
		ProductDTO dto = new ProductDTO();
		dto.setId(product.getId());
		dto.setName(product.getName());
		dto.setCategoryId(product.getCategory().getId());
		dto.setPrice(product.getPrice());
		dto.setStock(product.getStock());
		dto.setSizes(product.getSizes());
		dto.setDescription(product.getDescription());
		dto.setImageId(product.getImageId());
		return dto;
	}

	// " s, m ,xl " -> "S,M,XL"
	private String cleanSizes(String sizes) {
		if (sizes == null || sizes.isBlank()) {
			return null;
		}
		return String.join(",", java.util.Arrays.stream(sizes.split(","))
				.map(s -> s.trim().toUpperCase())
				.filter(s -> !s.isEmpty())
				.distinct()
				.toList());
	}
}
