package com.roommake.cart.service;

import com.roommake.cart.dto.CartItemDto;
import com.roommake.cart.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;

    /**
     * 로그인한 유저의 장바구니 상품 목록을 반환한다.
     *
     * @param userId 유저 번호
     * @return 로그인한 유저의 장바구니 상품 목록
     */
    @Transactional(readOnly = true)
    public List<CartItemDto> getCartsByUserId(int userId) {
        return cartMapper.getCartsByUserId(userId);
    }

    /**
     * 장바구니 상품의 상품 상세정보를 반환한다.
     *
     * @param productId 상품 번호
     * @return 상품번호에 해당하는 옵션 목록 및 상품 상세정보
     */
    @Transactional(readOnly = true)
    public List<CartItemDto> getItemOptionsByProductId(int productId) {
        return cartMapper.getItemOptionsByProductId(productId);
    }

    /**
     * 장바구니 상품을 삭제한다.
     *
     * @param cartIds 장바구니 상품 번호 리스트
     */
    public void deleteCart(List<Integer> cartIds) {
        cartMapper.deleteCart(cartIds);
    }

    /**
     * 장바구니 상품의 수량을 변경한다.
     *
     * @param cartId 장바구니 상품 번호
     * @param amount 장바구니 상품 수량
     */
    public void updateCartAmount(int cartId, int amount) {
        cartMapper.updateCartAmount(cartId, amount);
    }

    /**
     * 장바구니 상품의 옵션을 변경한다.
     *
     * @param cartId          장바구니 상품 번호
     * @param productDetailId 상품상세 번호
     */
    public void updateCartOption(int cartId, int productDetailId) {
        cartMapper.updateCartOption(cartId, productDetailId);
    }

    /**
     * 매일 자정에 추가한 지 30일이 경과한 장바구니 상품을 삭제한다.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredCartItems() {
        cartMapper.deleteExpiredCartItems();
    }
}
