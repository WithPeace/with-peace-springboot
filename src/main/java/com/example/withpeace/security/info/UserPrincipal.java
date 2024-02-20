package com.example.withpeace.security.info;

import com.example.withpeace.repository.UserRepository;
import com.example.withpeace.type.ERole;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPrincipal implements UserDetails, OAuth2User {
    @Getter private final Long id;      // 유저 정보 식별
    @Getter private final ERole role;   // 유저와 JWT 권환 확인
    private final String password;      // 유저의 비밀번호
    private final Collection<? extends GrantedAuthority> authorities;   // 인가에서 사용할 권한 넣을 곳
    private final Map<String, Object> attributes;   // OAuth2User 에서 사용할 정보 넣을 곳

    public static UserPrincipal create(UserRepository.UserSecurityForm form) {
        return UserPrincipal.builder()
                .id(form.getId())
                .role(form.getRole())
                .attributes(Collections.emptyMap())
                .authorities(Collections.singleton(new SimpleGrantedAuthority(form.getRole().toSecurityString()))).build();
    }

    public static UserPrincipal create(UserRepository.UserSecurityForm form, Map<String, Object> attributes) {
        return UserPrincipal.builder()
                .id(form.getId())
                .role(form.getRole())
                .attributes(attributes)
                .authorities(Collections.singleton(new SimpleGrantedAuthority(form.getRole().toSecurityString()))).build();
    }

    @Override
    public String getName() {
        return id.toString();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return id.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
