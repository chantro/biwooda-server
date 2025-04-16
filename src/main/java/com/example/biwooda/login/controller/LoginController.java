package com.example.biwooda.login.controller;

import com.example.biwooda.kakaoPay.service.KakaoFirestoreService;
import com.example.biwooda.umbrella.service.UmbrellaFirestoreService;
import com.example.biwooda.login.model.*;
import com.example.biwooda.login.service.EmailService;
import com.example.biwooda.login.service.LoginFirestoreService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/auth")
public class LoginController {
    @Autowired
    private EmailService emailService;
    @Autowired
    private LoginFirestoreService loginFirestoreService;
    @Autowired
    private UmbrellaFirestoreService umbrellaFirestoreService;
    @Autowired
    private KakaoFirestoreService kakaoFirestoreService;


    //이메일 인증 링크 보내기
    @PostMapping("/verify-email")
    public ResponseEntity<?> sendVerificationEmail(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        try {
            // 사용자 존재 여부 확인
            UserRecord userRecord;
            try {
                userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new EmailVerifyResponse(email, null, "User already exists"));
            } catch (FirebaseAuthException e) {
                if (e.getMessage().contains("No user record found")) {
                    // 사용자 존재하지 않음, 이메일 인증 진행
                    // 이메일 링크 발송
                    String code = emailService.sendEmail(email);
                    if(code != null) {
                        return ResponseEntity.ok(new EmailVerifyResponse(email, code, "Verification email sent"));
                    }else{
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new EmailVerifyResponse(email, code, "인증 이메일 전송에 실패했습니다."));
                    }
                } else {
                    throw e; // 다른 예외를 위해 다시 던짐
                }
            }
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new EmailVerifyResponse(email, null, e.getMessage()));
        }
    }

    //회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody EmailSignupRequest userRequest) {
        String email = userRequest.getEmail();
        String pwd = userRequest.getPwd();
        try {
            // 사용자 생성
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(pwd)
                    .setDisplayName(email);

            //회원 생성
            FirebaseAuth auth = FirebaseAuth.getInstance();
            UserRecord userRecord = auth.createUser(createRequest);

            //사용자 db 생성
            String uid = userRecord.getUid();
            loginFirestoreService.createUser(uid);

            return ResponseEntity.ok("User registered successfully: " + email);
        } catch (FirebaseAuthException | ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    //일반 이메일 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestHeader("Authorization") String authorization) {
        String idToken = authorization.replace("Bearer ", "");
        LoginResponse response = new LoginResponse();
        try {
            // ID 토큰 검증
            //FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            //String uid = decodedToken.getUid();
            String uid = "vLUQPUyNdFVZROB1eM5qBHHVF0o1";
            response.setIdToken(idToken);
            response.setMessage("Login Successfully");

            // 대여 정보 가져오기
            CompletableFuture<Boolean> future = kakaoFirestoreService.isAlreadyBorrow(uid);
            Boolean isAlreadyBorrow = future.get(); // 비동기 작업이 완료될 때까지 기다림

            if (isAlreadyBorrow) {
                response.setRentalState(true);
                CompletableFuture<Map<String, Object>> borrowedItemFuture = kakaoFirestoreService.getBorrowedItem(uid);
                Map<String, Object> borrowedItem = borrowedItemFuture.get(); // 비동기 작업이 완료될 때까지 기다림

                if (borrowedItem != null) {
                    // borrowedItem 데이터를 사용하여 응답 생성
                    String itemName = (String) borrowedItem.get("itemName");
                    String startDateStr = (String) borrowedItem.get("approved_at");
                    String endDateStr = (String) borrowedItem.get("limit_at");

                    response.setTicket(new Ticket(startDateStr, endDateStr, itemName));
                }
            } else {
                response.setRentalState(false);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


    // 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestHeader("Authorization") String authorization, @RequestBody ResetPasswordRequest resetRequest) {
        String idToken = authorization.replace("Bearer ", "");
        String newPassword = resetRequest.getNewPassword();
        try {
            // idToken을 검증하고 사용자 UID를 가져옴
            FirebaseAuth auth = FirebaseAuth.getInstance();
            //FirebaseToken decodedToken = auth.verifyIdToken(idToken);
            //String uid = decodedToken.getUid();
            String uid = "vLUQPUyNdFVZROB1eM5qBHHVF0o1";

            // 비밀번호 업데이트
            UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(uid)
                    .setPassword(newPassword);
            auth.updateUser(updateRequest);

            return ResponseEntity.ok(new ResetPasswordResponse("success", "Password reset successfully."));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResetPasswordResponse("failed", e.getMessage()));
        }
    }

    //회원 탈퇴
    @PostMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(@RequestHeader("Authorization") String authorization) {
        String idToken = authorization.replace("Bearer ", "");
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            //FirebaseToken decodedToken = auth.verifyIdToken(idToken);
            //String uid = decodedToken.getUid();
            String uid = "vLUQPUyNdFVZROB1eM5qBHHVF0o1";

            // 사용자 삭제 로직을 여기에 추가합니다.
            auth.deleteUser(uid);
            loginFirestoreService.deleteUser(uid);

            return ResponseEntity.ok("Account deleted successfully");
        } catch (FirebaseAuthException | ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
