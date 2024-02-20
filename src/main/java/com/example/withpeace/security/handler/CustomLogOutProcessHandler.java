package com.example.withpeace.security.handler;


import com.example.withpeace.repository.UserRepository;
import com.example.withpeace.security.info.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogOutProcessHandler implements LogoutHandler {
    private final UserRepository userRepository;
    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if(authentication == null){
            return;
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        userRepository.updateRefreshTokenAndLoginStatus(userPrincipal.getId(),null,false);

    }
}
