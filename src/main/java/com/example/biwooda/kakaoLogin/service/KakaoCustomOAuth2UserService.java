package com.example.biwooda.kakaoLogin.service;

import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class KakaoCustomOAuth2UserService extends DefaultOAuth2UserService {

    private final FirebaseCustomService firebaseCustomService;

    public KakaoCustomOAuth2UserService(FirebaseCustomService firebaseCustomService) {
        this.firebaseCustomService = firebaseCustomService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오 사용자 ID 가져오기
        String kakaoUserId = String.valueOf(attributes.get("id"));

        // Firebase 커스텀 토큰 생성
        String firebaseToken = "";
        try {
            if (StringUtils.hasText(kakaoUserId)) {
                firebaseToken = firebaseCustomService.createFirebaseToken(kakaoUserId);
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to create Firebase token", e);
        }

        // 사용자 정보에 Firebase 토큰 추가
        attributes.put("firebaseToken", firebaseToken);

        // DefaultOAuth2User를 사용하여 새로운 OAuth2User 생성
        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "id");
    }
}
