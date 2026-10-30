package com.threadkart.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.threadkart.store.service.NotFoundException;

@ControllerAdvice
public class ErrorAdvice {

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String notFound(NotFoundException e, Model model) {
		model.addAttribute("message", e.getMessage());
		return "error/404";
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public String tooBig(RedirectAttributes ra) {
		ra.addFlashAttribute("error", "That image is too large. Please keep it under 2 MB.");
		return "redirect:/admin/products";
	}
}
