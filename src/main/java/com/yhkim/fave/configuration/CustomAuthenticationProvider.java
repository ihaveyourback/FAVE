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

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.info("CustomAuthenticationProvider invoked for authentication");

        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails user = userDetailsService.loadUserByUsername(email);

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
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
