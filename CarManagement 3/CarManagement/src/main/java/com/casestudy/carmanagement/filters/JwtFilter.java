package com.casestudy.carmanagement.filters;

import com.casestudy.carmanagement.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Skip filtering for /api/cars/delete/** endpoints
    	System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Authorization Header: " + request.getHeader("Authorization"));


        String token = extractTokenFromRequest(request);
        if (token != null && jwtUtil.validateToken(token, jwtUtil.extractUsername(token))) {
            String role = jwtUtil.extractRole(token);
            if (role.startsWith("ROLE_")) {
                role = role.substring(5); // Remove 'ROLE_' prefix
            }

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(jwtUtil.extractUsername(token), null, AuthorityUtils.createAuthorityList(role));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);  // Extract token from "Bearer token"
        }
        return null;
    }
}



