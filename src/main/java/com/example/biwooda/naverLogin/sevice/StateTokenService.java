package com.example.biwooda.naverLogin.sevice;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;

@Service
public class StateTokenService {

    // CSRF 방지를 위한 상태 토큰 생성 메서드
    public static String generateState() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    // 생성된 상태 토큰을 세션에 저장
    public static void saveStateToken(HttpSession session) {
        String state = generateState();
        session.setAttribute("state", state);
    }

    public static String getStateToken(HttpSession session) {
        return (String) session.getAttribute("state");
    }
}
