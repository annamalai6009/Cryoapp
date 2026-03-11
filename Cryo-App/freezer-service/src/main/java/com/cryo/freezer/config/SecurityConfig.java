package com.cryo.freezer.config;

import com.cryo.freezer.filter.JwtAuthenticationFilter;
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

    @Value("${jwt.expiration:86400}")
    private Long jwtExpiration;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // if you want summary public:
                        // .requestMatchers("/freezers/summary/**").permitAll()
                        // others require JWT:
                        // ✅ ALLOW ADMINS ONLY
                        // ✅ ALLOW SWAGGER (Add this FIRST)
                        // //this line should  remove please rememeber Gopala.
                        .requestMatchers("/freezers/v3/api-docs", "/v3/api-docs", "/swagger-ui/**").permitAll()
                        //upto here i need to remove

                        .requestMatchers("/freezers/api/internal/**").permitAll() // ✅ ADD THIS
                        .requestMatchers("/freezers/admin/**").hasAnyRole("ADMIN","ROOT")
                        .requestMatchers("/freezers/**").authenticated()
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
        return new JwtAuthenticationFilter(jwtSecret, jwtExpiration);
    }


}
