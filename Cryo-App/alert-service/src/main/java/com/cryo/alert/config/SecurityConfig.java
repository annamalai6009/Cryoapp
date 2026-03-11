package com.cryo.alert.config;
import com.cryo.common.util.JwtTokenUtil;
import com.cryo.alert.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Default to 1 day if not set, just to be safe
    @Value("${jwt.expiration:86400}")
    private Long jwtExpiration;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Allow Swagger (Documentation)
                        //this line should  remove please rememeber Gopala.
                        .requestMatchers("/alerts/v3/api-docs", "/v3/api-docs", "/swagger-ui/**").permitAll()

                        // 2. ✅ NEW: Allow Freezer Service to trigger alerts internally
                        .requestMatchers("/alerts/evaluate").permitAll()


                        // ✅ ADD THIS LINE: Allow the Webhook
                        .requestMatchers("/webhook").permitAll()
                        .requestMatchers("/webhook/**").permitAll()

                        // 3. Protect everything else+
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Add the filter to validate JWTs from other services/gateway
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtSecret, jwtExpiration);
    }
}