package com.example.biwooda.umbrella.service;

import com.example.biwooda.kakaoPay.exception.FullUmbrellaLeftException;
import com.example.biwooda.kakaoPay.exception.NoUmbrellaLeftException;
import com.example.biwooda.kakaoPay.exception.NoUmbrellaLocker;
import com.example.biwooda.umbrella.model.UmbrellaLocker;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UmbrellaFirestoreService {
    private int initNum = 2;

    private final Firestore firestore;
    public UmbrellaFirestoreService(Firestore firestore) {
        this.firestore = firestore;
    }

    //lockerCode에 해당하는 우산함의 남은 우산 개수 업데이트
    public CompletableFuture<Integer> updateUmbrellaNum(String lockerCode, Boolean delete) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("umbrella/" + lockerCode + "/current_num");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer currentNum = dataSnapshot.getValue(Integer.class);
                    if(delete){
                        if(currentNum > 0){
                            currentNum -= 1;
                            ref.setValueAsync(currentNum);
                            future.complete(currentNum);
                        }else{
                            future.completeExceptionally(new NoUmbrellaLeftException());
                        }
                    }
                    else{
                      if(currentNum < initNum) {
                          currentNum += 1;
                          ref.setValueAsync(currentNum);
                          future.complete(currentNum);
                      }else{
                          future.completeExceptionally(new FullUmbrellaLeftException());
                      }
                    }
                } else {
                    future.completeExceptionally(new NoUmbrellaLocker());  // No data found
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new Exception("Failed to read value: "));
            }
        });
        return future;
    }

    //현재 해당 우산함에 우산함 반납이 가능한지 확인
    public CompletableFuture<Boolean> checkLocker(String lockerCode, Boolean delete){
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("umbrella/" + lockerCode + "/current_num");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer currentNum = dataSnapshot.getValue(Integer.class);
                    if(delete){
                        if(currentNum > 0){
                            future.complete(true);
                        }else{
                            future.completeExceptionally(new NoUmbrellaLeftException());
                        }
                    }
                    else{
                        if(currentNum < initNum) {
                            future.complete(true);
                        }else{
                            future.completeExceptionally(new FullUmbrellaLeftException());
                        }
                    }
                } else {
                    future.completeExceptionally(new NoUmbrellaLocker());  // No data found
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new Exception("Failed to read value: "));
            }
        });
        return future;
    }


    //우산함 정보 생성
    public CompletableFuture<Void> initUmbrella(String lockerCode, UmbrellaLocker umbrellaLocker) {
        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference("umbrella").child(lockerCode);

        CompletableFuture<Void> future = new CompletableFuture<>();

        ref.setValue(umbrellaLocker, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                future.completeExceptionally(databaseError.toException());
            } else {
                future.complete(null);
            }
        });

        return future;
    }
}
