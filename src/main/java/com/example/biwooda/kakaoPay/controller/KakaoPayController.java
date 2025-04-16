package com.example.biwooda.kakaoPay.controller;

import com.example.biwooda.kakaoPay.exception.*;
import com.example.biwooda.kakaoPay.model.*;
import com.example.biwooda.kakaoPay.service.KakaoPayService;
import com.example.biwooda.umbrella.service.UmbrellaFirestoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class KakaoPayController {
    private final KakaoPayService kakaoPayService;
    private final UmbrellaFirestoreService umbrellaFirestoreService;

    public KakaoPayController(KakaoPayService kakaoPayService, UmbrellaFirestoreService umbrellaFirestoreService) {
        this.kakaoPayService = kakaoPayService;
        this.umbrellaFirestoreService = umbrellaFirestoreService;
    }

    //request: 상품 정보 -> response: 결제창
    @PostMapping("/ready")
    public ResponseEntity<?> getRedirectUrl(@RequestHeader("Authorization") String authorization, @RequestBody KakaoPayItemInfo item) {
        String idToken = authorization.replace("Bearer ", "");
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(kakaoPayService.getRedirectUrl(idToken, item));
        } catch (NoUmbrellaLeftException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new KakaoBaseResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (AlreadyBorrowedException e){
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new KakaoBaseResponse(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage()));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    //ready request에 대한 결과 (pg_token 받음)
    @PostMapping("/success")
    public ResponseEntity<?> afterGetRedirectUrl(@RequestHeader("Authorization") String authorization, @RequestBody KakaoApproveRequest request){
        try{
            String idToken = authorization.replace("Bearer ", "");
            String pgToken = request.getPgToken();
            KakaoApproveResponse kakaoApprove = kakaoPayService.getApprove(pgToken, idToken);
            return ResponseEntity.status(HttpStatus.OK).body(kakaoApprove);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    //결제 진행 중 취소
    @GetMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestHeader("Authorization") String authorization, @RequestBody KakaoApproveRequest request){
        try {
            String idToken = authorization.replace("Bearer ", "");
            String lockerCode = request.getLockerCode();
            kakaoPayService.cancelReady(idToken, lockerCode);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new KakaoBaseResponse(HttpStatus.EXPECTATION_FAILED.value(), "사용자가 결제를 취소하였습니다."));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @GetMapping("/fail")
    public ResponseEntity<?> fail(@RequestHeader("Authorization") String authorization, @RequestBody KakaoApproveRequest request){
        try {
            String idToken = authorization.replace("Bearer ", "");
            String lockerCode = request.getLockerCode();
            kakaoPayService.failReady(idToken, lockerCode);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new KakaoBaseResponse(HttpStatus.EXPECTATION_FAILED.value(), "결제 실패"));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    //결제 취소(환불)
    @PostMapping("/borrow-cancel")
    public ResponseEntity<?> borrowCancel(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> request) {
        try {
            String idToken = authorization.replace("Bearer ", "");
            String lockerCode = request.get("lockerCode");
            return ResponseEntity.status(HttpStatus.OK)
                    .body(kakaoPayService.cancelBorrow(idToken, lockerCode));
        } catch (NotBorrowedException e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new KakaoBaseResponse(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    //우산 반납
    @PostMapping("/return")
    public ResponseEntity<?> umbrellaReturn(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> request) {
        try {
            String idToken = authorization.replace("Bearer ", "");
            String lockerCode = request.get("lockerCode");

            return ResponseEntity.status(HttpStatus.OK)
                    .body(kakaoPayService.returnUmbrella(idToken, lockerCode));
        } catch (NotBorrowedException | FullUmbrellaLeftException | NoUmbrellaLocker e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new KakaoBaseResponse(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KakaoBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

}

