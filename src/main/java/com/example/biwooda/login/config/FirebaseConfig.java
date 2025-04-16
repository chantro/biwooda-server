package com.example.biwooda.login.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

//Firebase Admin SDK 연동
@Configuration
public class FirebaseConfig {
    private FirebaseApp firebaseApp;

    //SDK 초기화
    @PostConstruct
    public void init(){
        try{
            //서비스 계정 키 가져오기
            FileInputStream serviceAccount = new FileInputStream(".\\src\\main\\resources\\biwooda-firebase-adminsdk-key.json");

            //option 구성
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://biwooda-smwu-default-rtdb.firebaseio.com/")
                    .build();

            //애플리케이션 시작 시, firebaseapp 초기화
            if (FirebaseApp.getApps().isEmpty()) {
                firebaseApp = FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
}