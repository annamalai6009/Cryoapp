package com.cryo.auth.config;
import com.cryo.auth.filter.JwtAuthenticationFilter;
import com.cryo.common.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.accessExpiration:900}")
    private Long accessExpiration;

    @Value("${jwt.refreshExpiration:604800}")
    private Long refreshExpiration;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // ❌ No CORS here (Gateway handles it)
                // 🔴 FORCE DISABLE CORS. This overrides any hidden configuration.
                .cors(cors -> cors.disable())

                .authorizeHttpRequests(auth -> auth
                        // 1. Allow Browser Pre-flight Checks (OPTIONS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Admin Endpoints
                        .requestMatchers("/auth/admin/**").hasAnyAuthority("ROOT", "ROLE_ROOT", "ADMIN", "ROLE_ADMIN")

                        // 3. Public Endpoints
                        .requestMatchers("/auth/login", "/auth/signup", "/auth/verify-otp", "/auth/resend-otp", "/auth/internal/**").permitAll()

                        // 🔴 CRITICAL FIX: Allow Freezer Service to fetch User Details by ID
                        .requestMatchers("/auth/users/**").permitAll()

                        // 4. Swagger
                        .requestMatchers("/auth/v3/api-docs/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // 5. Secured Profile Endpoint
                        .requestMatchers("/auth/profile").authenticated()

                        // 6. Everything else
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(jwtSecret, accessExpiration, refreshExpiration);
        return new JwtAuthenticationFilter(jwtTokenUtil);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
