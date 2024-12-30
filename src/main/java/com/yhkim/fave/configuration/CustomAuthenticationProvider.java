package com.yhkim.fave.configuration;

import com.yhkim.fave.exceptions.AccountDeletedException;
import com.yhkim.fave.exceptions.UserNotVerifiedException;
import com.yhkim.fave.exceptions.UserSuspendedException;
import com.yhkim.fave.services.SecurityUserDetailsService;
import com.yhkim.fave.entities.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private SecurityUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class); // 로거 객체 생성

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException { // 사용자 인증 메서드
        logger.info("CustomAuthenticationProvider invoked for authentication"); // 사용자 인증을 위해 CustomAuthenticationProvider가 호출됨

        String email = authentication.getName(); // 사용자가 입력한 이메일
        String password = authentication.getCredentials().toString(); // 사용자가 입력한 비밀번호

        UserDetails user = userDetailsService.loadUserByUsername(email); // 사용자 정보를 가져옴

        if (user instanceof UserEntity userEntity) {
            logger.info("Checking if account is deleted for user: {}", email);
            // 탈퇴된 계정인지 확인
            if (userEntity.getDeletedAt() != null) {
                logger.warn("Account deleted for user: {}", email);
                throw new AccountDeletedException("계정이 탈퇴되었습니다.");
            }

            if (!user.isEnabled()) {
                throw new UserNotVerifiedException("계정이 인증되지 않았습니다", userEntity);
            }

            if (!user.isAccountNonLocked()) {
                throw new UserSuspendedException("계정이 잠겼습니다");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다");
            }
        }

        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
        // 사용자 정보와 권한을 포함한 UsernamePasswordAuthenticationToken 객체 반환
    }

    @Override
    public boolean supports(Class<?> authentication) { // 지원하는 인증 객체인지 확인하는 메서드
        return authentication.equals(UsernamePasswordAuthenticationToken.class); // UsernamePasswordAuthenticationToken 클래스를 지원
    }
}
