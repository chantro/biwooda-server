package com.example.biwooda.kakaoLogin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
// redirect uri로 받은 인가 코드로 액세스 토큰 요청
public class KakaoTokenService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String requestAccessToken(String code, String clientId, String redirectUri) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // 요청 바디 설정
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("client_id", clientId);
        requestBody.add("redirect_uri", redirectUri);
        requestBody.add("code", code);

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 엔티티 생성
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // 액세스 토큰 요청 및 응답 처리
        ResponseEntity<String> responseEntity = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String accessToken = jsonNode.get("access_token").asText();
                return accessToken;
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse access token from Kakao");
            }
        } else {
            throw new RuntimeException("Failed to retrieve access token from Kakao");
        }
    }

    // 액세스 토큰으로부터 사용자 정보를 가져오는 메서드
    public String getKakaoUserId(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(userInfoUrl, HttpMethod.GET, requestEntity, String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String userId = jsonNode.get("id").asText();
                return userId;
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse user info from Kakao response", e);
            }
        } else {
            throw new RuntimeException("Failed to retrieve user info from Kakao");
        }
    }
}
