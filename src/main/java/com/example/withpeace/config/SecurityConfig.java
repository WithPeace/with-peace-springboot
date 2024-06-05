package com.example.withpeace.config;

import com.example.withpeace.constant.Constant;
import com.example.withpeace.security.JwtAuthEntryPoint;
import com.example.withpeace.security.JwtAuthenticationProvider;
import com.example.withpeace.security.filter.JwtAuthenticationFilter;
import com.example.withpeace.security.filter.JwtExceptionFilter;
import com.example.withpeace.security.handler.CustomLogOutProcessHandler;
import com.example.withpeace.security.handler.CustomLogOutResultHandler;
import com.example.withpeace.security.handler.JwtAccessDeniedHandler;
import com.example.withpeace.security.service.CustomUserDetailService;
import com.example.withpeace.type.ERole;
import com.example.withpeace.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailService customUserDetailService;

    private final JwtUtil jwtUtil;


    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private final CustomLogOutProcessHandler customLogOutProcessHandler;
    private final CustomLogOutResultHandler customLogOutResultHandler;

    @Bean
    protected SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity) throws Exception {


        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requestMatcherRegistry -> requestMatcherRegistry
                        .requestMatchers("/v3/api-docs/**", "/favicon.ico", "/configuration/**", "/swagger*/**", "/webjars/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("api/v1/auth/**").permitAll()
                        .requestMatchers("api/v1/users/profile/nickname/check").permitAll()
                        .requestMatchers("api/v1/users/**").hasAnyRole((ERole.USER.toString()),ERole.ADMIN.toString())
                        .requestMatchers("api/v1/admin/**").hasRole(ERole.ADMIN.toString())
                        .requestMatchers("/api/v1/posts/register").hasAnyRole((ERole.USER.toString()),ERole.ADMIN.toString())
                        .requestMatchers("/api/v1/app/check/android").permitAll()
                        .requestMatchers("/api/v1/app/setForceUpdateVersion/android").hasAnyRole(ERole.ADMIN.toString())
                        .anyRequest().authenticated())

                .logout(configurer ->
                        configurer
                                .logoutUrl("/api/v1/auth/logout")
                                .addLogoutHandler(customLogOutProcessHandler)
                                .logoutSuccessHandler(customLogOutResultHandler)
                                .deleteCookies(Constant.AUTHORIZATION,Constant.REAUTHORIZATION)
                )
                .exceptionHandling(configurer ->
                        configurer
                                .authenticationEntryPoint(jwtAuthEntryPoint)
                                .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, new JwtAuthenticationProvider(customUserDetailService)), LogoutFilter.class)
                .addFilterBefore(new JwtExceptionFilter(), JwtAuthenticationFilter.class)
                .getOrBuild();
    }
}
