package com.example.biwooda.kakaoLogin.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

// Firebase 커스텀 토큰 생성
@Service
public class FirebaseCustomService {

    private final Firestore firestore;

    public FirebaseCustomService(Firestore firestore) {
        this.firestore = firestore;
    }

    public String createFirebaseToken(String uid) throws FirebaseAuthException {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            // 사용자 존재, 커스텀 토큰 생성
            return FirebaseAuth.getInstance().createCustomToken(userRecord.getUid());
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setUid(uid);
                UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
                //사용자 db 생성
                //uid = userRecord.getUid();
                createUser(uid);
                return FirebaseAuth.getInstance().createCustomToken(userRecord.getUid());
            } else
                throw e;
        }
    }

    public void updateUser(String firebaseUid, String email, String name) throws FirebaseAuthException {
        UserRecord userRecord = FirebaseAuth.getInstance().getUser(firebaseUid);
        UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(userRecord.getUid())
                .setEmail(email)
                .setDisplayName(name);
        UserRecord updatedUser = FirebaseAuth.getInstance().updateUser(updateRequest);
        System.out.println("Updated user: " + updatedUser);
    }

    //회원가입 시, user 문서 생성
    public void createUser(String uid) {
        //users 콜렉션에 uid(Firebase auth의 uid)를 key로 사용자 문서 생성
        DocumentReference doc = firestore.collection("users").document(uid);
        //문서에 들어갈 contents
        Map<String, Object> contents = new HashMap<>();
        //현재 시간으로 time stamp
        long currentTimeMillis = System.currentTimeMillis();
        // 한국 시간에 맞춰서 포맷
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String formattedDate = sdf.format(new Date(currentTimeMillis));
        contents.put("created_at", formattedDate);
        ApiFuture<WriteResult> future = doc.set(contents);
        ApiFutures.addCallback(future, new ApiFutureCallback<WriteResult>() {
            @Override
            public void onSuccess(WriteResult result) {
                System.out.println("User document created successfully for UID: " + uid);
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Error adding user document for UID " + uid + ": " + t.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }
}
