package com.yhkim.fave.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public class CustomOAuth2User implements OAuth2User { // OAuth2User 인터페이스를 구현하는 사용자 클래스

    private final OAuth2User oAuth2User; // OAuth2User 객체
    private final String provider; // 제공자 이름
    private final String principalName; // 제공자 이름
    private final String nickname; // 닉네임
    private final String email; // 이메일
    private final String contact; // 연락처

    public CustomOAuth2User(OAuth2User oAuth2User, String provider, String principalName, String nickname, String contact, String email) { // 생성자 메서드
        this.oAuth2User = oAuth2User; // OAuth2User 객체
        this.provider = provider; // 제공자 이름
        this.principalName = principalName; // 제공자 이름
        this.nickname = nickname; // 닉네임
        this.contact = contact; // 연락처
        this.email = email; // 이메일
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // 권한을 가져오는 메서드
        return oAuth2User.getAuthorities();
    }

    @Override
    public Map<String, Object> getAttributes() { // 속성을 가져오는 메서드
        return oAuth2User.getAttributes();
    }

    @Override
    public String getName() { // 이름을 가져오는 메서드
        return oAuth2User.getName();
    }

}