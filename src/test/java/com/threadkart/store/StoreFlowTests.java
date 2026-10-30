package com.threadkart.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.threadkart.store.model.Order;
import com.threadkart.store.model.OrderStatus;
import com.threadkart.store.model.Product;
import com.threadkart.store.repository.OrderRepository;
import com.threadkart.store.repository.ProductRepository;
import com.threadkart.store.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoreFlowTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void homeAndShopPagesLoad() throws Exception {
		mvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Just added")));
		mvc.perform(get("/shop").param("q", "jeans")).andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Slim Fit Stretch Jeans")));
	}

	@Test
	void unknownProductGives404() throws Exception {
		mvc.perform(get("/shop/product/99999")).andExpect(status().isNotFound());
	}

	@Test
	void adminPagesAreProtected() throws Exception {
		mvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/login"));
		mvc.perform(get("/admin").with(user("someone@test.com").roles("USER"))).andExpect(status().isForbidden());
		mvc.perform(get("/admin").with(user("admin@test.com").roles("ADMIN"))).andExpect(status().isOk());
	}

	@Test
	void seededAdminCanLogIn() throws Exception {
		mvc.perform(SecurityMockMvcRequestBuilders.formLogin("/login").userParameter("email")
				.user("admin@test.com").password("admin123"))
				.andExpect(SecurityMockMvcResultMatchers.authenticated().withRoles("ADMIN"));
	}

	@Test
	void registerThenLogIn() throws Exception {
		mvc.perform(post("/register").with(csrf())
				.param("firstName", "Riya").param("lastName", "Sharma")
				.param("email", "riya@test.com")
				.param("password", "secret12").param("confirmPassword", "secret12"))
				.andExpect(redirectedUrl("/login"));

		// same email again should not create a second account
		mvc.perform(post("/register").with(csrf())
				.param("firstName", "Riya").param("email", "RIYA@test.com")
				.param("password", "secret12").param("confirmPassword", "secret12"))
				.andExpect(status().isOk());

		mvc.perform(SecurityMockMvcRequestBuilders.formLogin("/login").userParameter("email")
				.user("riya@test.com").password("secret12"))
				.andExpect(SecurityMockMvcResultMatchers.authenticated());
	}

	@Test
	@Transactional
	void addToCartCheckoutAndCancel() throws Exception {
		registerUser("buyer@test.com");
		Product jeans = findProduct("Slim Fit Stretch Jeans");
		int stockBefore = jeans.getStock();
		MockHttpSession session = new MockHttpSession();

		// sized product without a size -> back to product page
		mvc.perform(post("/cart/add").session(session).with(csrf()).param("productId", jeans.getId().toString()))
				.andExpect(redirectedUrl("/shop/product/" + jeans.getId()));

		mvc.perform(post("/cart/add").session(session).with(csrf())
				.param("productId", jeans.getId().toString()).param("size", "32").param("qty", "2"))
				.andExpect(redirectedUrl("/cart"));

		mvc.perform(get("/cart").session(session)).andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Slim Fit Stretch Jeans")));

		// invalid pincode -> form shown again, nothing saved
		mvc.perform(post("/checkout").session(session).with(csrf()).with(user("buyer@test.com"))
				.params(address("12")))
				.andExpect(status().isOk());
		assertThat(orderRepository.count()).isZero();

		mvc.perform(post("/checkout").session(session).with(csrf()).with(user("buyer@test.com"))
				.params(address("560001")))
				.andExpect(redirectedUrlPattern("/orders/TK-*"));

		List<Order> orders = orderRepository.findAll();
		assertThat(orders).hasSize(1);
		Order order = orders.get(0);
		assertThat(order.getItems()).hasSize(1);
		assertThat(order.getTotal()).isEqualByComparingTo("2998"); // above 999, so delivery is free
		assertThat(findProduct("Slim Fit Stretch Jeans").getStock()).isEqualTo(stockBefore - 2);

		// another customer can't see this order
		registerUser("other@test.com");
		mvc.perform(get("/orders/" + order.getOrderNumber()).with(user("other@test.com")))
				.andExpect(status().isNotFound());

		mvc.perform(post("/orders/" + order.getOrderNumber() + "/cancel").with(csrf()).with(user("buyer@test.com")))
				.andExpect(status().is3xxRedirection());
		assertThat(orderRepository.findByOrderNumber(order.getOrderNumber()).get().getStatus())
				.isEqualTo(OrderStatus.CANCELLED);
		assertThat(findProduct("Slim Fit Stretch Jeans").getStock()).isEqualTo(stockBefore);
	}

	@Test
	void outOfStockProductCantBeAdded() throws Exception {
		Product puffer = findProduct("Quilted Puffer Jacket");
		MockHttpSession session = new MockHttpSession();
		mvc.perform(post("/cart/add").session(session).with(csrf())
				.param("productId", puffer.getId().toString()).param("size", "M"))
				.andExpect(redirectedUrl("/shop/product/" + puffer.getId()));
		mvc.perform(get("/cart").session(session))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Your cart is empty")));
	}

	private void registerUser(String email) throws Exception {
		if (userRepository.existsByEmailIgnoreCase(email)) {
			return;
		}
		mvc.perform(post("/register").with(csrf())
				.param("firstName", "Test").param("email", email)
				.param("password", "secret12").param("confirmPassword", "secret12"));
	}

	private Product findProduct(String name) {
		return productRepository.findAll().stream().filter(p -> p.getName().equals(name)).findFirst().orElseThrow();
	}

	private org.springframework.util.MultiValueMap<String, String> address(String pincode) {
		var map = new org.springframework.util.LinkedMultiValueMap<String, String>();
		map.add("fullName", "Test Buyer");
		map.add("phone", "9876543210");
		map.add("addressLine1", "12 MG Road");
		map.add("city", "Bengaluru");
		map.add("state", "Karnataka");
		map.add("pincode", pincode);
		return map;
	}
}
