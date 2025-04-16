package com.example.biwooda.kakaoPay.service;

import com.example.biwooda.kakaoPay.exception.AlreadyBorrowedException;
import com.example.biwooda.kakaoPay.exception.NotBorrowedException;
import com.example.biwooda.kakaoPay.model.*;
import com.example.biwooda.umbrella.service.UmbrellaFirestoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class KakaoPayService {
    private final MakePayRequest makePayRequest;
    private final KakaoFirestoreService kakaoFirestoreService;
    private final UmbrellaFirestoreService umbrellaFirestoreService;
    private static final int UMBRELLA_PRICE = 10000;


    @Autowired
    public KakaoPayService(MakePayRequest makePayRequest, KakaoFirestoreService kakaoFirestoreService, UmbrellaFirestoreService umbrellaFirestoreService) {
        this.makePayRequest = makePayRequest;
        this.kakaoFirestoreService = kakaoFirestoreService;
        this.umbrellaFirestoreService = umbrellaFirestoreService;
    }

    //kakaoPay key
    @Value("${pay.kakao-admin-key}")
    private String adminKey;

    //클라이언트의 결제창을 응답으로 받기 위한 메소드
    //request: 상품 정보 / response: tid(결제 고유 번호) & 결제 URL
    @Transactional
    public KakaoReadyResponse getRedirectUrl(String idToken, KakaoPayItemInfo item) throws  Exception{
        //사용자 uid 가져오기
        //FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        //String uid = decodedToken.getUid();
        String uid = "vLUQPUyNdFVZROB1eM5qBHHVF0o1";
        String lockerCode = item.getLockerCode();

        //이미 대여중인 사용자인지 확인
        CompletableFuture<Boolean> future = kakaoFirestoreService.isAlreadyBorrow(uid);
        Boolean userExists = future.get(); // 여기서 CompletableFuture를 완료할 때까지 대기하고 결과를 가져옵니다.

        if (userExists) {
            throw new AlreadyBorrowedException();
        }

        //우산함 결함 확인
        CompletableFuture<Integer> umbrellaFuture = umbrellaFirestoreService.updateUmbrellaNum(lockerCode, true);
        try {
            Integer result = umbrellaFuture.get(); // 예외가 발생하면 여기서 던져짐
            // 다른 로직 수행
        } catch (ExecutionException e) {
            // CompletableFuture 내부의 예외를 다시 던져 컨트롤러에서 처리할 수 있도록 함
            throw (Exception) e.getCause();
        }

        //아니라면, 계속 진행
        //요청 header
        HttpHeaders headers = createKakaoHeaders();

        //요청 body
        KakaoPayRequest request = makePayRequest.getReadyRequest(uid, item);

        //Header와 Body 합쳐서 RestTemplate로 보내기 위한 밑작업
        HttpEntity<MultiValueMap<String, String>> urlRequest = new HttpEntity<>(request.getParameters(), headers);
        System.out.println(urlRequest);

        //RestTemplate로 Response 받아와서 DTO로 변환후 return
        RestTemplate rt = new RestTemplate();
        KakaoReadyResponse response = rt.postForObject(request.getUrl(), urlRequest, KakaoReadyResponse.class);
        System.out.println(response);

        //Firestore에 TID 저장
        assert response != null;
        kakaoFirestoreService.addTid(uid, response.getTid(), response);

        return response;
    }

    //클라이언트의 결제 승인 요청 메소드
    @Transactional
    public KakaoApproveResponse getApprove(String pgToken, String idToken) throws Exception {
        //사용자 uid, tid 가져오기
        //FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        //String uid = decodedToken.getUid();
        String uid = "vLUQPUyNdFVZROB1eM5qBHHVF0o1";
        String tid = kakaoFirestoreService.getTid(uid);

        //요청 header
        HttpHeaders headers = createKakaoHeaders();

        //요청 body
        KakaoPayRequest request=makePayRequest.getApproveRequest(uid, tid, pgToken);


        //Header와 Body 합쳐서 RestTemplate로 보내기 위한 밑작업
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(request.getParameters(), headers);

        // 요청 보내기
        RestTemplate rt = new RestTemplate();
        KakaoApproveResponse approveData = rt.postForObject(request.getUrl(), requestEntity, KakaoApproveResponse.class);

        //Firestore에 결제 정보 저장
        assert approveData != null;
        kakaoFirestoreService.addApproveData(uid, pgToken, approveData);
        kakaoFirestoreService.saveSid(uid, approveData);

        return approveData;
    }

    //결제 취소
    public void cancelReady(String idToken, String lockerCode) throws Exception {
        //사용자 uid, tid 가져오기
        //FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        //String uid = decodedToken.getUid();
        String uid = "vLUQPUyNdFVZROB1eM5qBHHVF0o1";
        String tid = kakaoFirestoreService.getTid(uid);

        kakaoFirestoreService.updateReadyCancel(uid, tid);
        CompletableFuture<Integer> umbrellaFuture = umbrellaFirestoreService.updateUmbrellaNum(lockerCode, false);
        try {
            Integer result = umbrellaFuture.get(); // 예외가 발생하면 여기서 던져짐
            // 다른 로직 수행
        } catch (ExecutionException e) {
            // CompletableFuture 내부의 예외를 다시 던져 컨트롤러에서 처리할 수 있도록 함
            throw (Exception) e.getCause();
        }
    }

    //결제 실패
    public void failReady(String idToken, String lockerCode) throws Exception {
        //사용자 uid, tid 가져오기
        ///FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        //String uid = decodedToken.getUid();
        String uid = "vLUQPUyNdFVZROB1eM5qBHHVF0o1";
        String tid = kakaoFirestoreService.getTid(uid);

        kakaoFirestoreService.updateReadyFailed(uid, tid);
        CompletableFuture<Integer> umbrellaFuture = umbrellaFirestoreService.updateUmbrellaNum(lockerCode, false);        try {
            Integer result = umbrellaFuture.get(); // 예외가 발생하면 여기서 던져짐
            // 다른 로직 수행
        } catch (ExecutionException e) {
            // CompletableFuture 내부의 예외를 다시 던져 컨트롤러에서 처리할 수 있도록 함
            throw (Exception) e.getCause();
        }
    }

    //결제 취소(환불)
    public Map<String, Object> cancelBorrow(String idToken, String lockerCode) throws Exception {
        Map<String, Object> cancelData = new HashMap<>();

        // 사용자 uid 가져오기 (임시로 하드코딩된 uid 사용)
        //FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        //String uid = decodedToken.getUid();
        String uid = "vLUQPUyNdFVZROB1eM5qBHHVF0o1";

        CompletableFuture<Void> future = kakaoFirestoreService.getBorrowedItem(uid).thenCompose(data -> {
            if (data != null) {
                // 요청 header
                HttpHeaders headers = createKakaoHeaders();

                // 요청 Body
                KakaoPayRequest request = makePayRequest.getCancelRequest(uid, data);

                // Header와 Body 합쳐서 RestTemplate로 보내기 위한 준비
                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(request.getParameters(), headers);

                // 요청 보내기
                RestTemplate rt = new RestTemplate();
                KakaoCancelResponse response = rt.postForObject(request.getUrl(), requestEntity, KakaoCancelResponse.class);
                cancelData.put("cancelResponse", response);

                if (response != null) {
                    return CompletableFuture.runAsync(() -> {
                        try {
                            kakaoFirestoreService.deleteBorrow(uid);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to delete data", e);
                        }
                    }).thenCompose(unused -> {
                        try {
                            return umbrellaFirestoreService.updateUmbrellaNum(lockerCode, false);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to update umbrella number", e);
                        }
                    }).thenCompose(updatedNum -> {
                        // Now call updateReadyCancel if everything else is successful
                        try {
                            kakaoFirestoreService.updateReadyCancel(uid, response.getTid()); // Assuming you have access to purchaseId
                        } catch (Exception e) {
                            throw new CompletionException(e);
                        }
                        cancelData.put("lockerCode", lockerCode);
                        cancelData.put("currentNum", updatedNum);
                        return CompletableFuture.completedFuture(null);
                    });
                } else {
                    throw new RuntimeException("Failed to cancel the borrow.");
                }
            } else {
                throw new CompletionException(new NotBorrowedException());
            }
        });

        future.join(); // Ensure the future is completed

        return cancelData;
    }

    //우산 반납 시 처음 요청하는 메소드
    public KakaoReturnResponse returnUmbrella(String idToken, String lockerCode) throws Exception {
        // 사용자 uid 가져오기
        // FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        // String uid = decodedToken.getUid();
        String uid = "vLUQPUyNdFVZROB1eM5qBHHVF0o1";
        CompletableFuture<KakaoReturnResponse> responseFuture = new CompletableFuture<>();

        CompletableFuture<Void> future = kakaoFirestoreService.getBorrowedItem(uid).thenCompose(data -> {   //대여중인 사용자인지 확인
            if (data != null) {
                // If the user has borrowed an item, proceed to check the locker
                return umbrellaFirestoreService.checkLocker(lockerCode, false).thenCompose(lockerAvailable -> {
                    if (lockerAvailable) {  //현재 우산함에 대여 슬롯이 존재하는지 확인
                        // If the locker is available, handle the return
                        return handleReturn(uid, data, lockerCode, responseFuture); //반납 프로세스 진행
                    } else {
                        throw new CompletionException(new Exception("locker is not available"));
                    }
                });
            } else {
                throw new CompletionException(new NotBorrowedException());
            }
        });

        future.join(); // Ensure the future is completed
        return responseFuture.get();
    }

    // 전체적인 반납 처리 관리 메소드
    private CompletableFuture<Void> handleReturn(String uid, Map<String, Object> data, String lockerCode, CompletableFuture<KakaoReturnResponse> responseFuture) {
        HttpHeaders headers = createKakaoHeaders();
        String sid = data.get("sid").toString();
        String tid = data.get("tid").toString(); // 처음 결제 시 tid 가져오기
        KakaoPayRequest request = makePayRequest.getReturnRequest(uid, data);
        LinkedMultiValueMap<String, String> map = request.getParameters();

        if (!map.get("total_amount").get(0).equals("0")) {
            return getReturnResponse(request, headers).thenCompose(response -> {
                KakaoReturnResponse returnResponse = new KakaoReturnResponse(response);
                return postReturnProcess(uid, lockerCode, tid, sid).thenAccept(updatedNum -> {
                    returnResponse.setCurrentNum(updatedNum);
                    returnResponse.setLockerCode(lockerCode);
                    responseFuture.complete(returnResponse);
                });
            });
        } else {
            KakaoReturnResponse response = createZeroAmountResponse(map, data);
            return postReturnProcess(uid, lockerCode, tid, sid).thenAccept(updatedNum -> {
                response.setCurrentNum(updatedNum);
                response.setLockerCode(lockerCode);
                responseFuture.complete(response);
            });
        }
    }

    // 카카오페이로 보낼 헤더 생성 메소드
    private HttpHeaders createKakaoHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = "KakaoAK " + adminKey;
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.set("Authorization", auth);
        return headers;
    }

    // 추가 결제 요청의 응답을 받는 메소드
    private CompletableFuture<KakaoApproveResponse> getReturnResponse(KakaoPayRequest request, HttpHeaders headers) {
        return CompletableFuture.supplyAsync(() -> {
            RestTemplate rt = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(request.getParameters(), headers);
            return rt.postForObject(request.getUrl(), requestEntity, KakaoApproveResponse.class);
        });
    }

    // 정상 반납일 때, response 생성 메소드
    private KakaoReturnResponse createZeroAmountResponse(LinkedMultiValueMap<String, String> map, Map<String, Object> data) {
        String now = LocalDateTime.now().toString();
        KakaoReturnResponse response = new KakaoReturnResponse();
        KakaoPayAmount amount = new KakaoPayAmount();
        amount.setTax(0);
        amount.setTaxFree(0);
        amount.setTotal(0);
        response.setAmount(amount);
        response.setSid(map.get("sid").get(0));
        response.setTid(data.get("tid").toString());
        response.setApproved_at(now);
        response.setCreated_at(now);
        response.setItem_name(map.get("item_name").get(0));
        return response;
    }

    // 반납 처리 프로세스
    private CompletableFuture<Integer> postReturnProcess(String uid, String lockerCode, String tid, String sid) {
        return umbrellaFirestoreService.updateUmbrellaNum(lockerCode, false).thenCompose(updatedNum -> {
            return CompletableFuture.runAsync(() -> {
                try {
                    kakaoFirestoreService.deleteBorrow(uid);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to delete data", e);
                }
            }).thenApply(unused -> updatedNum); // updatedNum을 다음 단계로 전달
        }).thenCompose(updatedNum -> {
            try {
                kakaoFirestoreService.updateReadyReturn(uid, tid);
                return CompletableFuture.completedFuture(updatedNum);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }).thenCompose(updatedNum -> {
            try {
                return inactiveSid(sid).thenApply(unused -> updatedNum);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
        /*return CompletableFuture.runAsync(() -> {
            try {
                kakaoFirestoreService.deleteBorrow(uid);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete data", e);
            }
        }).thenCompose(unused -> {
            return umbrellaFirestoreService.updateUmbrellaNum(lockerCode, false);
        }).thenCompose(updatedNum -> {
            try {
                kakaoFirestoreService.updateReadyReturn(uid, tid);
                return CompletableFuture.completedFuture(updatedNum);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }).thenCompose(updatedNum -> {
            try {
                return inactiveSid(sid).thenApply(unused -> updatedNum);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });*/
    }

    // 카카오페이 sid 비활성화 요청 메소드
    private CompletableFuture<Void> inactiveSid(String sid) {
        return CompletableFuture.runAsync(() -> {
            HttpHeaders headers = createKakaoHeaders();
            KakaoPayRequest deactivateRequest = makePayRequest.getInactiveRequest(sid);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(deactivateRequest.getParameters(), headers);
            RestTemplate rt = new RestTemplate();
            try {
                KakaoInactiveResponse response = rt.postForObject(deactivateRequest.getUrl(), requestEntity, KakaoInactiveResponse.class);
                System.out.println(response.toString());
            } catch (Exception e) {
                throw new RuntimeException("Failed to deactivate KakaoPay SID", e);
            }
        });
    }
}
