package com.threadkart.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.threadkart.store.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);
}
