package com.venturedive.application.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import org.springframework.boot.autoconfigure.security.oauth2.resource.JwtAccessTokenConverterConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Slf4j
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Override
	public void configure(ResourceServerSecurityConfigurer config) throws Exception {
		log.info("Configuring resource server..");
		config.stateless(true);
		config.resourceId("mobile-app");
		config.tokenStore(tokenStore());
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		Resource publicKey = new ClassPathResource("/public.key");
		try {
			converter.setVerifierKey(IOUtils.toString(publicKey.getInputStream()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		converter.setAccessTokenConverter(new JwtConverter());
		return converter;
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		log.info("Configuring http security..");
		http.antMatcher("/**").authorizeRequests().anyRequest().authenticated();
		http.oauth2ResourceServer().jwt();
		http.csrf().disable();
	}

	@Bean
	public JwtDecoder decoder() {
		return new TokenDecoder();
	}

	public static class JwtConverter extends DefaultAccessTokenConverter implements JwtAccessTokenConverterConfigurer {

		@Override
		public void configure(JwtAccessTokenConverter converter) {
			converter.setAccessTokenConverter(this);
		}

		@Override
		public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
			OAuth2Authentication auth = super.extractAuthentication(map);
			auth.setDetails(map); // this will get spring to copy JWT content
									// into Authentication
			return auth;
		}
	}
}
