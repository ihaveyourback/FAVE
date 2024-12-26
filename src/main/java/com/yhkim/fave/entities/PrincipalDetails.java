package com.yhkim.fave.entities;

import com.yhkim.fave.entities.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class PrincipalDetails implements OAuth2User, UserDetails { // OAuth2User와 UserDetails 인터페이스를 구현하는 사용자 클래스
    private final UserEntity user; // 사용자
    private final Map<String, Object> attributes; // 사용자 속성

    public PrincipalDetails(UserEntity user, Map<String, Object> attributes) { // 생성자 메서드
        this.user = user;// 사용자를 설정
        this.attributes = attributes; // 사용자 속성을 설정
    }

    @Override
    public Map<String, Object> getAttributes() { // 사용자 속성을 가져오는 메서드
        return attributes;
    }

    @Override
    public String getName() { // 사용자 이름을 가져오는 메서드
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // 사용자 권한을 가져오는 메서드
        return user.getAuthorities();
    }

    @Override
    public String getPassword() { // 사용자 비밀번호를 가져오는 메서드
        return user.getPassword();
    }

    @Override
    public String getUsername() { // 사용자 이름을 가져오는 메서드
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { // 계정이 만료되지 않았는지 확인
        return user.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() { // 계정이 잠기지 않았는지 확인
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() { // 자격 증명이 만료되지 않았는지 확인
        return user.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() { // 사용자가 활성화되었는지 확인
        return user.isEnabled();
    }
}