package com.roommake.admin.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ItemCancelDto {
    private int cancelId;       // 취소번호
    private int Id;
    private Date createDate;    // 취소 접수일
    private Date updtaeDate;    // 취소 수정일
    private int ReasonId;       // 취소 사유 번호
    private String ReasonName;  // 취소 사유
    private String productName; // 상품이름
    private String nickName;    // 신청자명
    private int productId;      // 취소상품번호
}
