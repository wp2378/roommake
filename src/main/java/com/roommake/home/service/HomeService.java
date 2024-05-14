package com.roommake.home.service;

import com.roommake.community.vo.Community;
import com.roommake.home.mapper.HomeMapper;
import com.roommake.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final HomeMapper homeMapper;

    /**
     * 신상품 4개를 조회한다.
     *
     * @return 신상품 4개의 정보가 담긴 Product 객체
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getNewProducts() {
        return homeMapper.getNewProducts();
    }

    /**
     * 조회수가 가장 높은 커뮤니티 '집들이' 글 4개를 조회한다.
     *
     * @return 조회수가 높은 커뮤니티 집들이 글 4개가 담긴 Community 객체
     */
    public List<Community> getCommHousePosts() {
        return homeMapper.getCommPostsByCategoryId(1);
    }

    /**
     * 조회수가 가장 높은 커뮤니티 '노하우' 글 4개를 조회한다.
     *
     * @return 조회수가 높은 커뮤니티 노하우 글 4개가 담긴 Community 객체
     */
    @Transactional(readOnly = true)
    public List<Community> getCommKnowhowPosts() {
        return homeMapper.getCommPostsByCategoryId(2);
    }

    /**
     * 로그인한 유저의 장바구니에 담긴 상품 개수를 조회한다.
     *
     * @param userId 유저 번호
     * @return 로그인 유저 장바구니에 담긴 상품 개수
     */
    @Transactional(readOnly = true)
    public int cartCountByUserId(int userId) {
        return homeMapper.cartCountByUserId(userId);
    }
}
