package com.example.biwooda.login.controller;

import com.example.biwooda.login.model.IdTokenResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class IdTokenController {
    private String idToken;
//    private Map<String, IdToken> tokenMap;

//    //클래스 생성 시, 먼저 실행
//    @PostConstruct
//    public void init() {
//        tokenMap = new HashMap<String, IdToken>();
//        tokenMap.put("sophia2359@sookmyung.ac.kr", new IdToken("Sophia's IdToken"));
//    }

    @PostMapping("/verifyToken")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> requestBody){
        idToken = requestBody.get("idToken");

        try{
            //클라이언트로부터 받은 토큰이 유효한 토큰인지 검증
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            //해당 계정이 관리자 계정인지 검증
            boolean isAdmin = checkAdmin(decodedToken);

            //관리자 계정인지 여부에 따라 admin 값 set
            if(isAdmin){
                setAdminCustomClaim(decodedToken.getUid(), true);
                return ResponseEntity.ok().body(new IdTokenResponse("true", "User is admin"));
            }else{
                setAdminCustomClaim(decodedToken.getUid(), false);
                return ResponseEntity.ok().body(new IdTokenResponse("true", "User is not admin"));
            }
        } catch (FirebaseAuthException e) {
            return ResponseEntity.badRequest().body("Failed to verify ID token: " + e.getMessage());
        }
    }

    private boolean checkAdmin(FirebaseToken decodedToken) {
        //계정의 이메일 주소 가져오기
        String email = decodedToken.getEmail();
        //이메일 유효성 검증
        boolean isEmailVerified = decodedToken.isEmailVerified();

        return email != null && isEmailVerified && email.equals("sophia2359@sookmyung.ac.kr");
    }

    private void setAdminCustomClaim(String uid, Boolean isAdmin) throws FirebaseAuthException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("admin", isAdmin);

        FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);
    }

}


