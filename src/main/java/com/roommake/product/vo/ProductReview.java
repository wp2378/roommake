package com.roommake.product.vo;

import com.roommake.order.vo.OrderItem;
import com.roommake.user.vo.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class ProductReview {

    private int id;                // 리뷰 번호
    private User userId;           // 유저 번호
    private OrderItem orderItemId; // 주문상세 번호
    private String content;        // 리뷰 내용
    private Date createDate;       // 리뷰 등록일
    private Date updateDate;       // 리뷰 수정일
    private int rating;            // 상품 별점
    private int voteCount;         // 상품 추천 누적수
}