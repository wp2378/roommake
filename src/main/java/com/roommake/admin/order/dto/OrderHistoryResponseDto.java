package com.roommake.admin.order.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderHistoryResponseDto {
    private int id;
    private int productId;         // 상품번호
    private int detailId;          // 상세번호
    private int price;             // 가격
    private int qty;               // 수량
    private int status;            //상태
    private String productName;    // 상품이름
    private String userNickname;  // 유저이름
    private String invoiceNumber; // 송장번호
}


