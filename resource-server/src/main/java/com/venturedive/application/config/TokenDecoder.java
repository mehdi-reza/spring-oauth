package com.venturedive.application.config;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenDecoder implements JwtDecoder {

	@Override
	public Jwt decode(String token) throws JwtException {
		log.info("Decoding token: {}", token);
		// no idea why spring needs this
		throw new JwtException("Not implemented ..");
	}
}
