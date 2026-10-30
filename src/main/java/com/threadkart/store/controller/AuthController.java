package com.threadkart.store.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.threadkart.store.dto.RegisterForm;
import com.threadkart.store.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/login")
	public String login(Principal principal) {
		if (principal != null) {
			return "redirect:/";
		}
		return "account/login";
	}

	@GetMapping("/register")
	public String registerForm(Model model, Principal principal) {
		if (principal != null) {
			return "redirect:/";
		}
		model.addAttribute("form", new RegisterForm());
		return "account/register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult result,
			RedirectAttributes ra) {
		if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "mismatch", "Passwords don't match");
		}
		if (form.getEmail() != null && !result.hasFieldErrors("email") && userService.emailTaken(form.getEmail())) {
			result.rejectValue("email", "taken", "An account with this email already exists");
		}
		if (result.hasErrors()) {
			return "account/register";
		}
		userService.register(form);
		ra.addFlashAttribute("success", "Account created. You can log in now.");
		return "redirect:/login";
	}
}
