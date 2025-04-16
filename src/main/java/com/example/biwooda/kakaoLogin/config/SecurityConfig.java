package com.example.biwooda.kakaoLogin.config;

import com.example.biwooda.kakaoLogin.service.KakaoCustomOAuth2UserService;
import com.example.biwooda.naverLogin.sevice.NaverCustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private KakaoCustomOAuth2UserService kakaoCustomOAuth2UserService;

    @Autowired
    private NaverCustomOAuth2UserService naverCustomOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/", "/auth", "/auth/**", "/login", "/auth/kakao/**", "/auth/login", "/auth/naver/**", "/payment/**").permitAll()
                        .anyRequest().authenticated() // 그 외의 요청은 인증이 필요
                );
                //.formLogin(Customizer.withDefaults()) // 기본 로그인 페이지
                //.logout(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserServiceRouter() {
        return request -> {
            String registrationId = request.getClientRegistration().getRegistrationId();
            if ("kakao".equals(registrationId)) {
                return kakaoCustomOAuth2UserService.loadUser(request);
            } else if ("naver".equals(registrationId)) {
                return naverCustomOAuth2UserService.loadUser(request);
            }
            throw new IllegalArgumentException("Unsupported client: " + registrationId);
        };
    }
}
