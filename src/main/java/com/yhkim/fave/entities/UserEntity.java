package com.yhkim.fave.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
//@Entity(name = "fave")
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

    @Column(nullable = false, unique = true, length = 10)
    private String nickname;

    @Column(nullable = false, unique = true, length = 12)
    private String contact;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @Column(name = "is_suspended", nullable = false)
    private boolean isSuspended;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "warning", length = 10)
    private int warning;


    @PrePersist
    protected void onCreate() {
        this.createAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }

    // 일반 로그인 시 가지고 가는 나의 id (identitiy)
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("IS_ADMIN")); // ROLE_* 제거
        }
        return authorities;
    }




    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isSuspended;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isVerified && !isSuspended;
    }

//    @Column(name = "oauth2_provider")
//    private String oauth2Provider; // 예: GOOGLE, FACEBOOK 등
//
//    @Column(name = "oauth2_id")
//    private String oauth2Id;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "email", this.email,
                "nickname", this.nickname,
                "contact", this.contact,
                "createAt", this.createAt,
                "updateAt", this.updateAt,
                "deletedAt", this.deletedAt,
                "isAdmin", this.isAdmin,
                "is_suspended", this.isSuspended,
                "is_verified", this.isVerified,
                "warning", this.warning
        );
    }

    // sns로그인할때 가져가는 이 유저의 id (identity)
    @Override
    public String getName() {
        return email;
    }


}