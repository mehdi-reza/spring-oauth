package com.venturedive.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/echo")
public class EchoController {
		
	@GetMapping
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public String index(Authentication auth) {
		OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
		return new StringBuffer(auth.toString()).append(" -- Decoded details: ").append(details.getDecodedDetails()).toString();
	}
}
