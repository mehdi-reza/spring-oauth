package com.venturedive.oauth.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;

import com.venturedive.oauth.SocialPlatform;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VdUserDetailsManager extends JdbcUserDetailsManager {
	
	private Function<ResultSet, UserDetails> resultSetExtractor() {
		return rs -> {
			try {
				if (rs.next()) {
					VdUserDetails shUser = new VdUserDetails(rs.getString(2), rs.getString(1), "ROLE_CUSTOMER");
					shUser.addAdditionalProperty("principal_name", rs.getString(3)); // uuid to add in token later via token enhancer
					return shUser;
				} else
					throw new RuntimeException("Invalid credentials..");
			} catch (SQLException e) {
				throw new UsernameNotFoundException(e.getMessage(), e);
			}
		};
	}

	public VdUserDetailsManager(DataSource dataSource) {
		setDataSource(dataSource);
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info("Loading user: {}", username);

		String query = loadUserQuery(username, Optional.empty());
		return executeQuery(query, new Object[] { username }, resultSetExtractor());
	}

	public UserDetails loadSocialUserByUsername(String username, SocialPlatform platform) throws UsernameNotFoundException {
		log.info("Loading social user: {}", username);

		String query = loadUserQuery(username, Optional.of(platform));
		return executeQuery(query, new Object[] { username }, resultSetExtractor());
	}

	/**
	 * Returns SQL query to load user
	 * @param username
	 * @param type
	 * @return
	 */
	private String loadUserQuery(String username, Optional<SocialPlatform> platform) {
		String column = username.matches(".*@.*") ? "email" : "mobile_number";
		return new StringBuilder("select password,").append(column).append(", principal_name from ")
				.append("users").append(" where ").append(column).append("=? ")
				.append("and account_type='").append(platform.isPresent() ? "SOCIAL_ID" : column.toUpperCase()).append("'").toString();
	}
	
	/**
	 * Execute query and delegate result set to extractor function
	 * @param query
	 * @param params
	 * @param f
	 * @return
	 */
	private UserDetails executeQuery(String query, Object[] params, final Function<ResultSet, UserDetails> f) {
		return getJdbcTemplate().query(query, params, new ResultSetExtractor<UserDetails>() {
			public UserDetails extractData(ResultSet rs) throws SQLException, DataAccessException {
				return f.apply(rs);
			}
		});
	}
}
