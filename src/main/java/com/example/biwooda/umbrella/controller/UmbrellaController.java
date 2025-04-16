package com.example.biwooda.umbrella.controller;

import com.example.biwooda.kakaoPay.model.KakaoBaseResponse;
import com.example.biwooda.umbrella.model.UmbrellaLocker;
import com.example.biwooda.umbrella.service.UmbrellaFirestoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/umbrella")
public class UmbrellaController {
    private final UmbrellaFirestoreService umbrellaFirestoreService;

    public UmbrellaController(UmbrellaFirestoreService umbrellaFirestoreService) {
        this.umbrellaFirestoreService = umbrellaFirestoreService;
    }

    //우산함 정보 생성
    @PostMapping("/locker-init")
    public ResponseEntity<?> test(@RequestHeader("Authorization") String authorization, @RequestBody UmbrellaLocker request) {
        try {
            String idToken = authorization.replace("Bearer ", "");
            String lockerCode = request.getLocker_code();
            umbrellaFirestoreService.initUmbrella(lockerCode, request);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
}
