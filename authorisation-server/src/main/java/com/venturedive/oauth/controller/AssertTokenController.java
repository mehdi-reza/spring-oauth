package com.venturedive.oauth.controller;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DelegatingOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import com.venturedive.oauth.config.VdUserDetailsManager;

import lombok.extern.slf4j.Slf4j;

/**
 * If we are provided with access token from google or facebook, then we should return our access token
 * @author mehdi
 *
 */
@RestController
@RequestMapping("oauth/assert")
@Slf4j
public class AssertTokenController implements InitializingBean {

	@Autowired 
	private RestTemplate template;
	
	@Autowired
	private VdUserDetailsManager userDetailsManager;
	
	private URI facebookRequestURI = null;
	private URI googleRequestURI = null;

	@Override
	public void afterPropertiesSet() throws Exception {
		googleRequestURI = new UriTemplate("https://content-people.googleapis.com/v1/people/me?personFields=emailAddresses,genders").expand("");
		facebookRequestURI = null;		
	}
	
	@Autowired
	@Qualifier("google")
	ClientRegistration google;
	
	@Autowired
	DelegatingOAuth2UserService<OAuth2UserRequest, OAuth2User> delegatingOAuth2UserService;
	
	@Autowired 
	DefaultTokenServices tokenServices;
	
	@Autowired
	ClientDetailsService clientDetailsService;
	
	@Autowired
	TokenEnhancerChain tokenEnhancerChain;

	@GetMapping(path="google/code")
	public @ResponseBody ResponseEntity<OAuth2AccessToken> googleWithCode(@RequestParam("code") String code) {
		
		OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> client = new DefaultAuthorizationCodeTokenResponseClient();
		
		OAuth2AuthorizationRequest codeRequest = 
				OAuth2AuthorizationRequest
				.authorizationCode()
				.clientId(google.getClientId())
				.scopes(google.getScopes())
				.authorizationUri(google.getProviderDetails().getAuthorizationUri())
				/*
				 * fixed state, and this should be used when authorization code is requested, we can treat this value as some kind of secret
				 * or a random state should be generated and passed on to this api end point
				 */				
				.state("state_parameter_passthrough_value")
				.redirectUri(google.getRedirectUriTemplate())
				.build();
		
		OAuth2AuthorizationResponse codeResponse = 
				OAuth2AuthorizationResponse.success(code)
				/*
				 * fixed state, and this should be used when authorization code is requested, we can treat this value as some kind of secret
				 * or a random state should be generated and passed on to this api end point
				 */				
				.state("state_parameter_passthrough_value")				
				.redirectUri("http://localhost")
				.build();
				
		// to initiate access token flow using authorization code
		OAuth2LoginAuthenticationToken accessTokenRequest = new OAuth2LoginAuthenticationToken(google, new OAuth2AuthorizationExchange(codeRequest, codeResponse));
		
		// check whether user exists in our system
		OAuth2LoginAuthenticationToken authenticated = (OAuth2LoginAuthenticationToken) new OAuth2LoginAuthenticationProvider(client, delegatingOAuth2UserService).authenticate(accessTokenRequest);

		// create access token from our system
		ClientDetails clientDetails = clientDetailsService.loadClientByClientId("mobile-app");
		DefaultOAuth2RequestFactory factory=new DefaultOAuth2RequestFactory(clientDetailsService);
		
		Collection<String> scopeAny = Arrays.asList(new String[] {"any"});
		OAuth2Authentication oauth2Authentication = new OAuth2Authentication(factory.createOAuth2Request(new AuthorizationRequest(clientDetails.getClientId(), scopeAny)), authenticated);
		
		OAuth2AccessToken accessToken =  tokenServices.createAccessToken(oauth2Authentication);
		
		// apply enhancements, so a JWT token can be generated
		accessToken = tokenEnhancerChain.enhance(accessToken, oauth2Authentication);
		
		return new ResponseEntity<OAuth2AccessToken>(accessToken, HttpStatus.OK);
	}
}
