package com.example.biwooda.kakaoPay.service;

import com.example.biwooda.kakaoPay.model.KakaoPayItemInfo;
import com.example.biwooda.kakaoPay.model.KakaoPayRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.time.temporal.ChronoUnit;


@Component
@RequiredArgsConstructor
public class MakePayRequest {
    static final String cid = "TCSUBSCRIP"; // 가맹점 테스트 코드
    //@Autowired
    //private HttpServletRequest request;

    //결제 준비 요청 데이터 생성 메소드
    public KakaoPayRequest getReadyRequest(String uid, KakaoPayItemInfo itemInfo) {
        String orderId = "point" + uid;
        int num = itemInfo.getNum();
        int price = itemInfo.getPrice();
        int total_amount = num * price;
        String lockerCode = itemInfo.getLockerCode();

        // 요청의 도메인 추출
        //String baseUrl = request.getRequestURL().toString();
        //String domain = baseUrl.substring(0, baseUrl.indexOf(request.getRequestURI()));
	String domain = "https://biwooda.vercel.app";

        //카카오페이 요청 양식
        LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("cid", cid);
        parameters.add("partner_order_id", orderId); //가맹점 주문번호
        parameters.add("partner_user_id", uid);  //가맹점 회원 id
        parameters.add("item_name", itemInfo.getItemName());  //상품명
        parameters.add("quantity", num + "");  //상품 수량
        parameters.add("total_amount", total_amount + "");  //상품 총액
        parameters.add("tax_free_amount", "0");  //상품 비과세 금액
        parameters.add("approval_url", domain + "/payment/success" + "/" + uid + "/" + lockerCode); // 성공 시 redirect url
        parameters.add("cancel_url", domain + "/payment/cancel" + "/" + uid + "/" + lockerCode); // 취소 시 redirect url
        parameters.add("fail_url", domain + "/payment/fail" + "/" + uid + "/" + lockerCode); // 실패 시 redirect url

        return new KakaoPayRequest("https://kapi.kakao.com/v1/payment/ready", parameters);
    }

    //결제 승인 요청 생성 메소드
    public KakaoPayRequest getApproveRequest(String uid, String tid, String pgToken){
        LinkedMultiValueMap<String,String> map=new LinkedMultiValueMap<>();

        String orderId="point"+uid;
        // 가맹점 코드 테스트코드는 TCSUBSCRIP 이다.
        map.add("cid", "TCSUBSCRIP");

        // getReadyRequest 에서 받아온 tid
        map.add("tid", tid);
        map.add("partner_order_id", orderId); // 주문명
        map.add("partner_user_id", uid);

        // getReadyRequest에서 받아온 redirect url에 클라이언트가
        // 접속하여 결제를 성공시키면 아래의 url로 redirect 되는데
        //http://localhost:4000/payment/success"+"/"+id
        // 여기에 &pg_token= 토큰값 이 붙어서 redirect 된다.
        // 해당 내용을 뽑아 내서 사용하면 된다.
        map.add("pg_token", pgToken);

        return new KakaoPayRequest("https://kapi.kakao.com/v1/payment/approve",map);
    }

    //결제 취소(환불) 요청 생성 메소드
    public KakaoPayRequest getCancelRequest(String uid, Map<String, Object> item){
        LinkedMultiValueMap<String,String> map=new LinkedMultiValueMap<>();

        // 가맹점 코드 테스트코드는 TCSUBSCRIP 이다.
        map.add("cid", "TCSUBSCRIP");

        // getReadyRequest 에서 받아온 tid
        map.add("tid", (String) item.get("tid"));
        map.add("cancel_amount", item.get("amount")+"");
        map.add("cancel_tax_free_amount", "0");

        return new KakaoPayRequest("https://kapi.kakao.com/v1/payment/cancel", map);
    }

    //환불 요청 생성 메소드
    public KakaoPayRequest getReturnRequest(String uid, Map<String, Object> item){
        LinkedMultiValueMap<String,String> map=new LinkedMultiValueMap<>();
        int amount = 0;
        //현재시간
        LocalDateTime now = LocalDateTime.now();
        // approved_at을 LocalDateTime으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime limitAt= LocalDateTime.parse((String) item.get("limit_at"), formatter);

        // 지각 처리
        if (!now.isBefore(limitAt)) {
            System.out.println("late");
            //long minutesLate = Duration.between(limitAt, now).toMinutes();
            int daysLate = (int) (ChronoUnit.DAYS.between(limitAt.toLocalDate(), now.toLocalDate()));
            int[] extraList = getExtraReturn();

            amount = extraList[daysLate-1];  //1~14일 요금
            // amount = (int) ((minutesLate / 60 + 1) * 100); // 1시간당 100원
            //amount = (int) ((minutesLate/2 + 1) * 100); // 2분당 100원
        }

        String orderId="point"+uid;
        // 가맹점 코드 테스트코드는 TCSUBSCRIP 이다.
        map.add("cid", "TCSUBSCRIP");

        map.add("sid",(String) item.get("sid"));
        map.add("partner_order_id",orderId);
        map.add("partner_user_id",uid);
        map.add("item_name",(String) item.get("itemName"));
        map.add("quantity","1");
        System.out.println("price: " + amount);
        map.add("total_amount",amount+"");
        map.add("tax_free_amount", "0");

        return new KakaoPayRequest("https://kapi.kakao.com/v1/payment/subscription", map);
    }

    private int[] getExtraReturn(){
        int[] extraList ={600, 1200, 1800, 2400, 3000, 3668, 4461, 5318, 6244, 7243, 8323, 9489, 10748, 12107};

        return extraList;
    }


    //sid 비활성화 요청 생성 메소드
    public KakaoPayRequest getInactiveRequest(String sid){
        LinkedMultiValueMap<String,String> map=new LinkedMultiValueMap<>();

        // 가맹점 코드 테스트코드는 TCSUBSCRIP 이다.
        map.add("cid", "TCSUBSCRIP");

        // getReadyRequest 에서 받아온 tid
        map.add("sid", sid);

        return new KakaoPayRequest("https://kapi.kakao.com/v1/payment/manage/subscription/inactive", map);
    }

}
