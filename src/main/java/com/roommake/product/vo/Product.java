package com.roommake.product.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Product {

    private int id;                     // 상품번호
    private String name;                // 상품이름
    private String content;             // 상품설명
    private String statusYn;            // 판매상태
    private Date createDate;            // 상품 등록일
    private Date updateDate;            // 상품 수정일
    private ProductCategory categoryId; // 카테고리번호
    private String deleteYn;            // 상품삭제여부
    private int parentsId;              // 추가상품번호
}
