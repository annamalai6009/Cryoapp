package com.cryo.freezer.filter;
import com.cryo.common.util.JwtTokenUtil;
import com.cryo.freezer.util.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT auth filter for freezer-service.
 * It validates ACCESS tokens only (the one sent in Authorization header).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Keep constructor signature the same as before (uses JwtTokenUtil(secret, expiration))
     */
    public JwtAuthenticationFilter(String jwtSecret, Long jwtExpiration) {
        this.jwtTokenUtil = new JwtTokenUtil(jwtSecret, jwtExpiration);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header = {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            String token = authHeader.substring(7).trim();

            if (!token.isEmpty()) {
                try {
                    // <-- Use the new access-token validator
                    if (jwtTokenUtil.validateAccessToken(token)) {

                        // Extract standard claims
                        String ownerUserId = null;
                        String email = null;
                        String mobileNumber = null;
                        String role = null;

                        try {
                            ownerUserId = jwtTokenUtil.extractOwnerUserId(token);
                        } catch (Exception e) {
                            logger.warn("Unable to extract ownerUserId from token: {}", e.getMessage());
                        }

                        try {
                            email = jwtTokenUtil.extractEmail(token);
                        } catch (Exception e) {
                            logger.warn("Unable to extract email from token: {}", e.getMessage());
                        }

                        try {
                            mobileNumber = jwtTokenUtil.extractMobileNumber(token);
                        } catch (Exception e) {
                            // optional claim
                        }

                        try {
                            role = jwtTokenUtil.extractRole(token);
                        } catch (Exception e) {
                            logger.warn("Unable to extract role from token: {}", e.getMessage());
                        }

                        logger.debug("JWT valid. ownerUserId={}, email={}, mobile={}, role={}",
                                ownerUserId, email, mobileNumber, role);

                        if (ownerUserId != null && role != null) {
                            // store values in ThreadLocal for this request
                            UserContext.setUserId(ownerUserId);
                            UserContext.setEmail(email);
                            UserContext.setMobileNumber(mobileNumber);

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            ownerUserId,
                                            null,
                                            Collections.singletonList(
                                                    new SimpleGrantedAuthority("ROLE_" + role)
                                            )
                                    );

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            logger.warn("JWT missing required claims (ownerUserId or role). Skipping authentication.");
                        }
                    } else {
                        logger.warn("JWT validation failed or token is not an access token.");
                    }
                } catch (Exception e) {
                    logger.warn("JWT validation error: {}", e.getMessage(), e);
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // clean ThreadLocal to avoid leakage
            UserContext.clear();
        }
    }
}
