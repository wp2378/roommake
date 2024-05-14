package com.roommake.admin.order.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class ItemReturnDto {

    private int itemReturnId;                   // 반품번호
    private Date itemReturnCreateDate;          // 반품신청일
    private Date itemReturnUpdateDate;          // 반품수정일
    private String itemReturnStatus;            // 반품상태
    private int orderItemId;
    private String itemReturnYn;                // 반품여부
    private int returnExchangeReasonId;         // 상품사유번호
    private int collectionDeliveryId;
    private String returnExchangeReasonName;    // 반품사유
    private int orderId;                        // 주문번호
    private int orderItemPrice;                 // 상품가격
    private String userNickname;                // 신청자명
    private String productName;                 // 상품이름
    private int productId;
}
