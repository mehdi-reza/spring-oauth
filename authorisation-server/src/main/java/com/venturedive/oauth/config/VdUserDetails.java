package com.venturedive.oauth.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;

public class VdUserDetails implements UserDetails {

	/**
	 * 
	 */
	
	private Collection<? extends GrantedAuthority> authorities;
	
	private static final long serialVersionUID = -1378991736284474552L;
	
	private Map<String, Object> additionalProperties=new HashMap<>();

	private String password;

	private String username;

	private boolean accountExpired = false;

	private boolean accountNonLocked = true;

	private boolean isCredentialsNonExpired = true;

	private boolean enabled = true;
	
	public VdUserDetails(String username, String password, String ... authorities) {
		this.username=username;
		this.password=password;
		this.authorities=AuthorityUtils.createAuthorityList(authorities);
	}

	public void addAdditionalProperty(String key, String value) {
		additionalProperties.put(key, value);
	}
	
	public void removeAdditionalProperty(String key, String value) {
		additionalProperties.remove(key);
	}
	
	public Object getAdditionalProperty(String key) {
		return additionalProperties.get(key);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return !this.accountExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.isCredentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}
}
