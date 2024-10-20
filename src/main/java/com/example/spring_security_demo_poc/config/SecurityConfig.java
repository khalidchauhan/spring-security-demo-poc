package com.example.spring_security_demo_poc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.spring_security_demo_poc.filter.JwtRequestFilter;
import com.example.spring_security_demo_poc.service.CustomUserDetailsService;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CustomUserDetailsService userDetailsService;

	private final JwtRequestFilter jwtRequestFilter;

	public SecurityConfig(CustomUserDetailsService userDetailsService, JwtRequestFilter jwtRequestFilter) {
		this.userDetailsService = userDetailsService;
		this.jwtRequestFilter = jwtRequestFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection
				.authorizeHttpRequests(auth -> auth.requestMatchers("/register", "/login").permitAll() // Allow public access
																									// to the
																									// authenticate
																									// endpoint
						.anyRequest().authenticated() // All other requests need authentication
				).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use
																												// stateless
																												// session
																												// management
				).cors(cors -> {
					// Define CORS configuration
					CorsConfiguration configuration = new CorsConfiguration();
					configuration.setAllowCredentials(true);
					configuration.addAllowedOrigin("http://localhost:4200"); // Allow your Angular app
					configuration.addAllowedHeader("*");
					configuration.addAllowedMethod("*");

					UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
					source.registerCorsConfiguration("/**", configuration);

					// Set the CORS configuration
					cors.configurationSource(source);
				});;

		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		return authenticationManagerBuilder.build();
	}
}