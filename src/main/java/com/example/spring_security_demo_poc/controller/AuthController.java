package com.example.spring_security_demo_poc.controller;

import com.example.spring_security_demo_poc.dto.AuthResponse;
import com.example.spring_security_demo_poc.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_security_demo_poc.entity.User;
import com.example.spring_security_demo_poc.service.CustomUserDetailsService;
import com.example.spring_security_demo_poc.util.JwtUtil;

@RestController
public class AuthController {
	private final AuthenticationManager authenticationManager;

	private final JwtUtil jwtUtil;

	private final CustomUserDetailsService userDetailsService;

	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> authenticate(@RequestBody User user) throws Exception {
		try {
			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
		} catch (Exception e) {
			throw new CustomException("Invalid username or password");
		}

		final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
		final String accessToken = jwtUtil.generateToken(userDetails.getUsername());
		final String refreshToken = jwtUtil.generateRefreshToken(userDetails);
		return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshAccessToken(@RequestBody String refreshToken) {
		String username = jwtUtil.extractUsername(refreshToken);
		if (username != null && !jwtUtil.isTokenExpired(refreshToken)) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			String newAccessToken = jwtUtil.generateToken(userDetails.getUsername());
			return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid refresh token");
	}
}
