package com.yhkim.fave.configuration;

import com.yhkim.fave.exceptions.UserNotVerifiedException;
import com.yhkim.fave.exceptions.UserSuspendedException;
import com.yhkim.fave.services.SecurityUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebSecurityConfigurer {

    @Autowired
    private SecurityUserDetailsService userDetailsService;

    @Lazy // 의존성 주입 지연
    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // UserDetailsService 및 CustomAuthenticationProvider 설정
        http
                .authenticationProvider(customAuthenticationProvider) // 커스텀 AuthenticationProvider 추가
                .userDetailsService(userDetailsService) // 기본 UserDetailsService는 여전히 사용 가능
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .rememberMe(remember -> remember
                        .tokenValiditySeconds(14 * 24 * 60 * 60)
                        .key(System.getenv("SECURITY_REMEMBER_ME_KEY")) // 환경 변수로 가져오기
                        .userDetailsService(userDetailsService)
                )
                .csrf().disable()
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/assets/**").permitAll()
                                .requestMatchers("/user/profile").authenticated()
                                .requestMatchers("/user/**").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/**").permitAll()
                                .requestMatchers("/api/login").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/")
                        .loginProcessingUrl("/api/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"result\": \"success\"}");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            String failureReason = "failure";
                            int statusCode = 401;
                            if (exception instanceof UserNotVerifiedException) {
                                failureReason = "failure_not_verified";
                                statusCode = 403;
                            } else if (exception instanceof UserSuspendedException) {
                                failureReason = "failure_suspended";
                                statusCode = 403;
                            } else if (exception instanceof BadCredentialsException) {
                                failureReason = "failure";
                                statusCode = 401;
                            }
                            response.setStatus(statusCode);
                            response.getWriter().write("{\"result\": \"" + failureReason + "\"}");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void init(SecurityBuilder builder) {
        // 초기화 작업
    }

    @Override
    public void configure(SecurityBuilder builder) {
        // 설정 작업
    }
}