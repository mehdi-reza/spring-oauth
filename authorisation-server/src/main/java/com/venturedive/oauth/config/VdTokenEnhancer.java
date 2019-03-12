package com.venturedive.oauth.config;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VdTokenEnhancer implements TokenEnhancer {

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

		DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) accessToken;
		
		// it is populated from the password grant flow
		if(authentication.getPrincipal().getClass().isAssignableFrom(VdUserDetails.class)) {
			VdUserDetails principal = (VdUserDetails) authentication.getPrincipal();
			token.setAdditionalInformation(principal.getAdditionalProperties());
		// logged in from google/facebook	
		} else if(authentication.getPrincipal().getClass().isAssignableFrom(DefaultOAuth2User.class)) {
			DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
			token.setAdditionalInformation(principal.getAttributes());
		} else
			throw new RuntimeException("Could not identify authenticated principal..");

		return token;
	}
}
