package com.venturedive.oauth.registrations;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.stereotype.Component;

@Component
@Qualifier("google")
public class Google implements FactoryBean<ClientRegistration> {

	@Override
	public ClientRegistration getObject() throws Exception {
		return ClientRegistration.withRegistrationId("google")
				.clientId("70732567463-4k841le5j9gf6u6ufvldle1niaf24fcr.apps.googleusercontent.com")
				.clientSecret("1RNbuf6djaXlU05f10CVJ_aO")
				// .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUriTemplate("http://localhost")
				.scope("profile", "email")
				.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
				.tokenUri("https://www.googleapis.com/oauth2/v4/token")
				.userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
				.userNameAttributeName(IdTokenClaimNames.SUB).jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
				.clientName("google").build();
	}

	@Override
	public Class<ClientRegistration> getObjectType() {
		return ClientRegistration.class;
	}
}
