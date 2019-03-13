package com.venturedive.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/echo")
public class EchoController {
		
	@GetMapping
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public String index(Authentication auth) {
		return "Greetings from Spring Boot!";
	}
}
