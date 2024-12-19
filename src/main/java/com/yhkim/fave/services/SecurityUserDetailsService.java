package com.yhkim.fave.services;

import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 이메일로 사용자 검색
        UserEntity userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        logger.info("User found: {}", userEntity.getEmail());

        // UserEntity가 UserDetails를 구현했으므로 바로 반환
        return userEntity;
    }
}
