package com.threadkart.store.config;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.threadkart.store.model.Category;
import com.threadkart.store.model.Product;
import com.threadkart.store.model.ProductImage;
import com.threadkart.store.model.Role;
import com.threadkart.store.model.User;
import com.threadkart.store.repository.CategoryRepository;
import com.threadkart.store.repository.ProductImageRepository;
import com.threadkart.store.repository.ProductRepository;
import com.threadkart.store.repository.UserRepository;

/**
 * Runs once on startup:
 *  - makes sure an admin account exists
 *  - adds a few sample products when the store is empty (so the live demo doesn't look blank)
 */
@Component
public class DataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final ProductImageRepository imageRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.email}")
	private String adminEmail;

	@Value("${app.admin.password}")
	private String adminPassword;

	@Value("${app.seed-demo-data}")
	private boolean seedDemoData;

	public DataSeeder(UserRepository userRepository, CategoryRepository categoryRepository,
			ProductRepository productRepository, ProductImageRepository imageRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) throws Exception {
		createAdmin();
		if (seedDemoData && productRepository.count() == 0) {
			addSampleProducts();
		}
	}

	private void createAdmin() {
		if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
			return;
		}
		User admin = new User();
		admin.setFirstName("Admin");
		admin.setEmail(adminEmail.toLowerCase());
		admin.setPassword(passwordEncoder.encode(adminPassword));
		admin.setRole(Role.ADMIN);
		userRepository.save(admin);
		log.info("Created admin account {}", adminEmail);
	}

	private void addSampleProducts() throws IOException {
		Category tees = category("T-Shirts");
		Category shirts = category("Shirts");
		Category bottoms = category("Bottoms");
		Category winter = category("Winterwear");

		product("Classic Crew Neck Tee - Charcoal", tees, 499, 40, "S,M,L,XL",
				"Everyday cotton tee with a relaxed fit. 180 GSM, pre-shrunk, doesn't lose shape after a few washes.",
				"tee-charcoal.png");
		product("Classic Crew Neck Tee - Mustard", tees, 499, 25, "S,M,L,XL",
				"Same comfy crew neck in a warm mustard shade. Pairs well with denim or chinos.",
				"tee-mustard.png");
		product("Striped Pocket Tee", tees, 649, 18, "M,L,XL",
				"Breton-style stripes with a chest pocket. 100% combed cotton.",
				"tee-striped.png");
		product("Oxford Button-Down Shirt", shirts, 1199, 15, "S,M,L,XL,XXL",
				"Soft oxford cotton, button-down collar. Works for office days and weekends.",
				"shirt-oxford.png");
		product("Olive Linen Shirt", shirts, 1399, 3, "M,L,XL",
				"Breathable linen blend for hot afternoons. Slightly cropped sleeves look good rolled up.",
				"shirt-linen.png");
		product("Slim Fit Stretch Jeans", bottoms, 1499, 22, "28,30,32,34,36",
				"Mid-wash denim with 2% stretch so you can actually sit down comfortably.",
				"jeans-blue.png");
		product("Beige Chinos", bottoms, 1299, 12, "30,32,34,36",
				"Tapered chinos in a neutral beige. Flat front, side pockets, belt loops.",
				"chinos-beige.png");
		product("Fleece Pullover Hoodie", winter, 1799, 10, "S,M,L,XL",
				"Brushed fleece inside, kangaroo pocket, double-layer hood. Warm without being bulky.",
				"hoodie-maroon.png");
		product("Quilted Puffer Jacket", winter, 2999, 0, "M,L,XL",
				"Lightweight puffer with a water-resistant shell. Packs into its own pocket.",
				"jacket-puffer.png");
		log.info("Added sample products");
	}

	private Category category(String name) {
		return categoryRepository.save(new Category(name));
	}

	private void product(String name, Category category, int price, int stock, String sizes,
			String description, String imageFile) throws IOException {
		Product p = new Product();
		p.setName(name);
		p.setCategory(category);
		p.setPrice(BigDecimal.valueOf(price));
		p.setStock(stock);
		p.setSizes(sizes);
		p.setDescription(description);

		ClassPathResource resource = new ClassPathResource("seed-images/" + imageFile);
		if (resource.exists()) {
			try (InputStream in = resource.getInputStream()) {
				ProductImage image = imageRepository.save(new ProductImage(in.readAllBytes(), "image/png"));
				p.setImageId(image.getId());
			}
		}
		productRepository.save(p);
	}
}
