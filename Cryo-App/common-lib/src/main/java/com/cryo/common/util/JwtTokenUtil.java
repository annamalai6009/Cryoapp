package com.cryo.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JwtTokenUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    private final String secret;
    private final Long accessExpiration;   // in seconds
    private final Long refreshExpiration;  // in seconds

    /**
     * New constructor: separate access + refresh expiry.
     */
    public JwtTokenUtil(String secret, Long accessExpiration, Long refreshExpiration) {
        this.secret = secret;
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * Backward compatible constructor (used by other services).
     * - Treat the given expiration as access token expiry
     * - Refresh token = 7x access expiry (can be anything you like)
     */
    public JwtTokenUtil(String secret, Long expiration) {
        this(secret, expiration, expiration * 7);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /* ------------------------------------------------------------------
       TOKEN GENERATION
       ------------------------------------------------------------------ */

    private Map<String, Object> commonClaims(String ownerUserId, String email, String role, String mobileNumber) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("ownerUserId", ownerUserId);
        claims.put("role", role);
        if (mobileNumber != null) {
            claims.put("mobileNumber", mobileNumber);
        }
        return claims;
    }

    /**
     * ✅ Generate ACCESS token
     */
    public String generateAccessToken(String ownerUserId, String email, String role, String mobileNumber) {
        Map<String, Object> claims = commonClaims(ownerUserId, email, role, mobileNumber);
        claims.put("type", "access");
        return createToken(claims, email, accessExpiration);
    }

    /**
     * ✅ Generate REFRESH token
     */
    public String generateRefreshToken(String ownerUserId, String email, String role, String mobileNumber) {
        Map<String, Object> claims = commonClaims(ownerUserId, email, role, mobileNumber);
        claims.put("type", "refresh");
        return createToken(claims, email, refreshExpiration);
    }

    /**
     * Old method kept for compatibility.
     * It now behaves like "generateAccessToken".
     */
    @Deprecated
    public String generateToken(String ownerUserId, String email, String role, String mobileNumber) {
        return generateAccessToken(ownerUserId, email, role, mobileNumber);
    }

    @Deprecated
    public String generateToken(String ownerUserId, String email, String role) {
        return generateAccessToken(ownerUserId, email, role, null);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationSeconds * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /* ------------------------------------------------------------------
       EXTRACTION
       ------------------------------------------------------------------ */

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractOwnerUserId(String token) {
        return extractClaim(token, claims -> claims.get("ownerUserId", String.class));
    }

    public String extractMobileNumber(String token) {
        return extractClaim(token, claims -> claims.get("mobileNumber", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Error parsing JWT token", e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /* ------------------------------------------------------------------
       VALIDATION
       ------------------------------------------------------------------ */

    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Boolean validateAccessToken(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            return "access".equalsIgnoreCase(extractTokenType(token))
                    && tokenEmail.equals(email)
                    && !isTokenExpired(token);
        } catch (Exception e) {
            logger.error("Error validating access token", e);
            return false;
        }
    }

    public Boolean validateAccessToken(String token) {
        try {
            return "access".equalsIgnoreCase(extractTokenType(token))
                    && !isTokenExpired(token);
        } catch (Exception e) {
            logger.error("Error validating access token", e);
            return false;
        }
    }

    public Boolean validateRefreshToken(String token) {
        try {
            return "refresh".equalsIgnoreCase(extractTokenType(token))
                    && !isTokenExpired(token);
        } catch (Exception e) {
            logger.error("Error validating refresh token", e);
            return false;
        }
    }
}
