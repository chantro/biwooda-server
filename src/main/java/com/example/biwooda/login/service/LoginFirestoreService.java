package com.example.biwooda.login.service;

import com.example.biwooda.login.config.FirebaseConfig;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class LoginFirestoreService {
    private final Firestore firestore;

    public LoginFirestoreService(Firestore firestore) {
        this.firestore = firestore;
    }

    //회원가입 시, user 문서 생성
    public void createUser(String uid) throws ExecutionException, InterruptedException {
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

        //성공 여부 확인
        ApiFuture<WriteResult> result = doc.set(contents);
        result.get();
    }

    //회원 정보 삭제
    public void deleteUser(String uid) throws ExecutionException, InterruptedException {
        DocumentReference doc = firestore.collection("users").document(uid);
        deleteDocument(doc);
        deleteCollection(firestore, "users", uid);
        //성공 여부 확인
    }

    private void deleteDocument(DocumentReference docRef) {
        ApiFuture<WriteResult> future = docRef.delete();
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    //회원 정보 하위 컬렉션 삭제
    private void deleteCollection(Firestore db, String collectionName, String uid) {
        DocumentReference userDocRef = db.collection(collectionName).document(uid);

        // Retrieve all sub-collections
        Iterable<CollectionReference> subCollections = userDocRef.listCollections();

        try {
            // Iterate through each sub-collection
            for (CollectionReference subCollection : subCollections) {
                ApiFuture<QuerySnapshot> future = subCollection.get();
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();

                // Iterate through each document in the sub-collection and delete it
                for (QueryDocumentSnapshot document : documents) {
                    deleteDocument(document.getReference());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
