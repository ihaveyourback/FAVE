package com.yhkim.fave.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(schema = "fave", name = "users")
@ToString
@EqualsAndHashCode(of = {"email"})
public class UserEntity implements UserDetails, OAuth2User {

    @Id
    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 10)
    private String nickname; // Removed unique = true

    @Column(nullable = false, unique = true, length = 50)
    private String contact;

    @Column(name = "create_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "update_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_admin")
    private boolean admin;

    @Column(name = "is_suspended", nullable = false)
    private boolean suspended = false;

    @Column(name = "is_verified")
    private boolean verified;

    @Column
    private int warning;

    @Column(name = "oauth2_provider", length = 50)
    private String oauth2Provider;

    public boolean isSocialLogin() { // 소셜 로그인 여부 확인하는 메서드
        return oauth2Provider != null && !oauth2Provider.isEmpty(); // 소셜 로그인 여부 확인
    }

    @Column(name = "oauth2_id", length = 50)
    private String oauth2Id;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getUsername() {
        return email;
    } // 사용자 이름을 가져오는 메서드

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // 권한을 가져오는 메서드
        List<GrantedAuthority> authorities = new ArrayList<>(); // 권한을 저장할 리스트 객체 생성
        if (admin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); // 관리자 권한 추가
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 사용자 권한 추가
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    } // 계정이 만료되지 않았는지 확인하는 메서드

    @Override
    public boolean isAccountNonLocked() {
        return !suspended;
    } // 계정이 잠기지 않았는지 확인하는 메서드

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    } // 자격이 만료되지 않았는지 확인하는 메서드

    @Override
    public boolean isEnabled() {
        return verified && !suspended && deletedAt == null;
    } // 사용자가 활성화되었는지 확인하는 메서드

    @Override
    public Map<String, Object> getAttributes() { // 속성을 가져오는 메서드
        Map<String, Object> attributes = new HashMap<>();   // 속성을 저장할 맵 객체 생성
        attributes.put("email", this.email);
        attributes.put("nickname", this.nickname);
        attributes.put("contact", this.contact);
        attributes.put("createdAt", this.createdAt);
        attributes.put("updatedAt", this.updatedAt);
        attributes.put("deletedAt", this.deletedAt);
        attributes.put("admin", this.admin);
        attributes.put("suspended", this.suspended);
        attributes.put("verified", this.verified);
        attributes.put("warning", this.warning);
        attributes.put("oauth2Provider", this.oauth2Provider);
        attributes.put("oauth2Id", this.oauth2Id);
        return attributes;
    }

    @Override
    public String getName() {
        return email;
    } // 이름을 가져오는 메서드

    public boolean isSuspended() {
        return suspended;
    } // suspended 속성에 대한 getter 메서드 추가
}