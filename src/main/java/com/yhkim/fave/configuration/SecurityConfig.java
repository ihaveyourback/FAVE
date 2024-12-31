package com.yhkim.fave.configuration;

import com.yhkim.fave.exceptions.OAuth2IdNotFoundException;
import com.yhkim.fave.services.OAuth2MemberService;
import com.yhkim.fave.services.SecurityUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

import java.io.IOException;

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

    @Lazy
    @Autowired
    private OAuth2MemberService oAuth2MemberService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(customAuthenticationProvider)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .rememberMe(remember -> remember
                        .tokenValiditySeconds(14 * 24 * 60 * 60)
                        .key(System.getenv("SECURITY_REMEMBER_ME_KEY") != null ? System.getenv("SECURITY_REMEMBER_ME_KEY") : "defaultRememberMeKey")
                        .userDetailsService(userDetailsService)
                )
                .csrf().disable()
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/assets/**").permitAll()
                                .requestMatchers("/profile/**").authenticated()
                                .requestMatchers("/user/secession").authenticated()
                                .requestMatchers("/user/**").permitAll()
                                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
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
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/")
                        .userInfoEndpoint()
                        .userService(oAuth2MemberService)
                        .and()
                        .failureHandler(new ExceptionMappingAuthenticationFailureHandler() {
                            @Override
                            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                                saveException(request, exception);
                                if (exception instanceof OAuth2IdNotFoundException) {
                                    getRedirectStrategy().sendRedirect(request, response, "/user/error?error=oauth2IdNotFound");
                                } else if (exception instanceof OAuth2AuthenticationException) {
                                    getRedirectStrategy().sendRedirect(request, response, "/user/error?error=oauth2_deleted");
                                } else {
                                    super.onAuthenticationFailure(request, response, exception);
                                }
                            }
                        })
                );
        return http.build();
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() { //
        return new SecurityUserDetailsService();
    }
}