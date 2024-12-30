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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityUserDetailsService userDetailsService;// 사용자 정보 서비스

    @Lazy// 지연 로딩
    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;// 사용자 인증 프로바이더

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;// 사용자 인증 실패 핸들러

    @Lazy// 지연 로딩
    @Autowired
    private OAuth2MemberService oAuth2MemberService;// OAuth2 사용자 서비스

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { // 보안 필터 체인
        http
                .authenticationProvider(customAuthenticationProvider)// 사용자 인증 프로바이더
                .sessionManagement(session -> session// 세션 관리
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)// 세션 생성 정책
                        .maximumSessions(1)// 최대 세션 수
                        .maxSessionsPreventsLogin(false) // 중복 로그인 방지
                )
                .rememberMe(remember -> remember // 로그인 유지 설정
                        .tokenValiditySeconds(14 * 24 * 60 * 60) //     14일
                        .key(System.getenv("SECURITY_REMEMBER_ME_KEY") != null ? System.getenv("SECURITY_REMEMBER_ME_KEY") : "defaultRememberMeKey") // 기본 키
                        .userDetailsService(userDetailsService) // 사용자 정보 서비스
                )
                .csrf().disable()
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/assets/**").permitAll() // 정적 자원
                                .requestMatchers("/profile/**").authenticated() // 프로필
                                .requestMatchers("/user/**").permitAll() // 사용자
                                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN") // 관리자 페이지 (권한) // ROLE_ADMIN 권한 필요
                                .requestMatchers("/api/**").permitAll() // API
                                .requestMatchers("/api/login").permitAll() // 로그인 API
                                .anyRequest().authenticated() // 그 외
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
                            response.getWriter().write("{\"result\": \"success\"}"); // 로그인 성공 시 응답
                        })
                        .failureHandler(customAuthenticationFailureHandler) // 로그인 실패 시 핸들러
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 URL
                        .logoutSuccessUrl("/") // 로그아웃 성공 시 리다이렉트 URL
                        .invalidateHttpSession(true)// 세션 무효화
                        .clearAuthentication(true)// 인증 정보 삭제
                        .deleteCookies("JSESSIONID")
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/") // 로그인 페이지
                        .userInfoEndpoint()
                        .userService(oAuth2MemberService)
                        .and()
                        .failureHandler(new ExceptionMappingAuthenticationFailureHandler() {


                            @Override
                            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse  response, AuthenticationException exception) throws IOException, ServletException { // 인증 실패 시 호출되는 메서드 (오버라이딩)
                                saveException(request, exception); // 예외 저장
                                System.out.println("onAuthenticationFailure INVOKED");
                                if (exception instanceof OAuth2IdNotFoundException) { // OAuth2IdNotFoundException 예외가 발생한 경우 (OAuth2IdNotFoundException)
                                    System.out.println("!!! Thrown exception instances of OAuth2IdNotFoundException.");
                                    getRedirectStrategy().sendRedirect(request, response, "/user/error?error=oauth2IdNotFound");  // 로그인 실패 시 홈으로 리다이렉트 (OAuth2IdNotFoundException)
                                } else {
                                    super.onAuthenticationFailure(request, response, exception);  // 기본 실패 핸들러 호출 (OAuth2IdNotFoundException)
                                }
                            }
                        })
                );
        return http.build();
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {// 사용자 인증 매니저
        auth.authenticationProvider(customAuthenticationProvider);// 사용자 인증 프로바이더
    }

    @Bean
    public PasswordEncoder passwordEncoder() {// 비밀번호 인코더
        return new BCryptPasswordEncoder();// BCrypt 암호화 방식
    }
}
