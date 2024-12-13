package com.yhkim.fave.services;

import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.mappers.UserMapper;
import com.yhkim.fave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
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

    //    public static void main(String[] args) {
//        // 컨텍스트를 통해 빈을 생성하는 대신 수동으로 인스턴스를 만듭니다.
//        SecurityUserDetailsService service = new SecurityUserDetailsService();
//
//        // 수동으로 주입 - 실제로 이 부분은 Spring이 수행합니다.
//        // service.userMapper = new UserMapper(); // UserMapper의 구현체 인스턴스를 주입해야 함.
//
//        try {
//            UserDetails user = service.loadUserByUsername("test@example.com");
//            System.out.println(user);
//        } catch (UsernameNotFoundException e) {
//            System.out.println("User not found: " + e.getMessage());
//        }
//    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 이메일로 사용자 검색
        UserEntity userEntity = (UserEntity) userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // UserEntity 반환
        return userEntity;
    }
}