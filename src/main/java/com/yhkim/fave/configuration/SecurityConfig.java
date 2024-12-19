package com.yhkim.fave.configuration;

import com.yhkim.fave.services.SecurityUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityUserDetailsService userDetailsService;

    @Lazy
    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(customAuthenticationProvider) // 커스텀 AuthenticationProvider 추가
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1) // 세션 최대 1개
                        .maxSessionsPreventsLogin(false)
                )
                .rememberMe(remember -> remember
                        .tokenValiditySeconds(14 * 24 * 60 * 60) // rememberMe 유효 기간 14일
                        .key(System.getenv("SECURITY_REMEMBER_ME_KEY") != null ? System.getenv("SECURITY_REMEMBER_ME_KEY") : "defaultRememberMeKey")
                        .userDetailsService(userDetailsService)
                )
                .csrf().disable()
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/assets/**").permitAll()  // 공개된 리소스
                                .requestMatchers("/profile/**").authenticated()  // 인증된 사용자만 접근 가능
                                .requestMatchers("/user/**").permitAll()  // 로그인 페이지 등 공개된 리소스
                                .requestMatchers("/admin/**").hasAuthority("IS_ADMIN") // 관리자만 접근
                                .requestMatchers("/api/**").permitAll()  // API 경로 공개
                                .requestMatchers("/api/login").permitAll()  // 로그인 경로 공개
                                .anyRequest().authenticated()  // 나머지 모든 경로는 인증된 사용자만 접근 가능
                )
                .formLogin(form -> form
                        .loginPage("/")  // 로그인 페이지 경로
                        .loginProcessingUrl("/api/login")  // 로그인 처리 URL
                        .defaultSuccessUrl("/")  // 로그인 성공 시 리디렉션
                        .permitAll()
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"result\": \"success\"}");
                        })
                        .failureHandler(customAuthenticationFailureHandler)  // 커스텀 실패 핸들러 추가
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

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customAuthenticationProvider); // 커스텀 AuthenticationProvider 설정
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
