package com.venturedive.oauth.config;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.provisioning.UserDetailsManager;

import com.venturedive.oauth.SocialPlatform;

import lombok.extern.slf4j.Slf4j;

/**
 * Entry point to transform oauth2 user details retrieved from user info end
 * point We utilize our user details service to load user from database after
 * extracting the user name from data returned from google and facebook
 * 
 * @author mehdi
 *
 */
@Slf4j
public class VdOAuth2UserService extends DefaultOAuth2UserService {

	private UserDetailsManager userDetailsManager;

	private Map<String, Function<OAuth2User, String>> factory = new HashMap<>();

	// return user name from google user info
	private Function<OAuth2User, String> google = oauth2User -> {
		/*
		 * [{metadata={primary=true, verified=true, source={type=ACCOUNT, id=112625921177483781372}}, value=mehdi.reza@gmail.com}]
		 */
		return oauth2User.getAttributes().get("email").toString();
	};
	
	// return user name from facebook user info
	private Function<OAuth2User, String> facebook = oauth2User -> {
		return null;
	};

	public VdOAuth2UserService(UserDetailsManager userDetailsManager) {
		this.userDetailsManager = userDetailsManager;
		factory.put("google", google);
		factory.put("facebook", facebook);
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		// this will hit the user info endpoint of google or facebook
		OAuth2User oAuth2User = super.loadUser(userRequest);

		VdUserDetailsManager vdUserDetailsManager = (VdUserDetailsManager)userDetailsManager;
		
		String providerName = userRequest.getClientRegistration().getClientName();
		
		// every social platform has different naming convention of user name
		String userName = factory.get(userRequest.getClientRegistration().getClientName()).apply(oAuth2User);
				
		UserDetails userDetails = vdUserDetailsManager
				.loadSocialUserByUsername(userName, SocialPlatform.valueOf(providerName.toUpperCase()));

		// add principal name into oauth2User attributes so we can retrieve it later in token enhancer
		// the following point can be used to add additional properties of our user which can be used later
		VdUserDetails shUserDetails = (VdUserDetails)userDetails;
		Map<String,Object> attributes=new Hashtable<>(oAuth2User.getAttributes());
		
		attributes.put("principal_name", shUserDetails.getAdditionalProperty("principal_name"));
		
		return new DefaultOAuth2User(userDetails.getAuthorities(), attributes, "email");
	}
}
