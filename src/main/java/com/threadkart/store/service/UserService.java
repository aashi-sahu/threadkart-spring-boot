package com.threadkart.store.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.threadkart.store.dto.RegisterForm;
import com.threadkart.store.model.Role;
import com.threadkart.store.model.User;
import com.threadkart.store.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmailIgnoreCase(email.trim())
				.orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
		return new org.springframework.security.core.userdetails.User(
				user.getEmail(), user.getPassword(),
				List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
	}

	public boolean emailTaken(String email) {
		return userRepository.existsByEmailIgnoreCase(email.trim());
	}

	public User register(RegisterForm form) {
		User user = new User();
		user.setFirstName(form.getFirstName().trim());
		user.setLastName(form.getLastName() == null ? null : form.getLastName().trim());
		user.setEmail(form.getEmail().trim().toLowerCase());
		user.setPassword(passwordEncoder.encode(form.getPassword()));
		user.setRole(Role.USER);
		return userRepository.save(user);
	}

	public User getByEmail(String email) {
		return userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new NotFoundException("User not found"));
	}

	public long countUsers() {
		return userRepository.count();
	}
}
