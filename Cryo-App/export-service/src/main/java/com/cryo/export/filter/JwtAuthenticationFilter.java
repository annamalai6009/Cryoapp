package com.cryo.export.filter;

import com.cryo.common.util.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Validate ACCESS tokens from Authorization header.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationFilter(String jwtSecret, Long jwtExpiration) {
        this.jwtTokenUtil = new JwtTokenUtil(jwtSecret, jwtExpiration);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            String token = authHeader.substring(7).trim();

            if (!token.isEmpty()) {
                try {
                    // <-- use new access-token validator
                    if (jwtTokenUtil.validateAccessToken(token)) {
                        String role = null;
                        String ownerUserId = null;

                        try {
                            role = jwtTokenUtil.extractRole(token);
                        } catch (Exception ignored) {
                        }
                        try {
                            ownerUserId = jwtTokenUtil.extractOwnerUserId(token);
                        } catch (Exception ignored) {
                        }

                        if (ownerUserId != null && role != null) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            ownerUserId,
                                            null,
                                            Collections.singletonList(
                                                    new SimpleGrantedAuthority("ROLE_" + role)
                                            )
                                    );

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (Exception ignored) {
                    // swallow - filter should not crash request pipeline
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
