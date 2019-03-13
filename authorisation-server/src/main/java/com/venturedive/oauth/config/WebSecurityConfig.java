package com.venturedive.oauth.config;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DelegatingOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.core.user.OAuth2User;

@EnableAuthorizationServer
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	DataSource dataSource;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private VdUserDetailsManager userDetailsService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	@Override
	public UserDetailsService userDetailsServiceBean() throws Exception {
		return userDetailsService;
	}

	// delegating to multiple user services, which ever returns an OAuth2User
	// first will be consumed
	@Bean
	public DelegatingOAuth2UserService<OAuth2UserRequest, OAuth2User> delegatingOAuth2UserService() {
		
		List<OAuth2UserService<OAuth2UserRequest, OAuth2User>> services = new ArrayList<>();
		services.add(new VdOAuth2UserService(userDetailsService));

		DelegatingOAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2userServices = new DelegatingOAuth2UserService<>(services);		
		return oAuth2userServices;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable();


		http.oauth2Login().authorizationEndpoint().baseUri("/oauth2/authz");

		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http.oauth2Login().redirectionEndpoint().baseUri("/oauth2/exchange/*");

		http.oauth2Login().userInfoEndpoint().userAuthoritiesMapper(new SimpleAuthorityMapper());
		
		//http.oauth2Client().clientRegistrationRepository(registrations());
	}
	
	/*@Autowired 
	@Qualifier("google")
	ClientRegistration google;
	
	public ClientRegistrationRepository registrations() {
		
		return new ClientRegistrationRepository() {
			
			@Override
			public ClientRegistration findByRegistrationId(String registrationId) {
				// add a check for facebook
				return google;
			}
		};
	}*/
}
