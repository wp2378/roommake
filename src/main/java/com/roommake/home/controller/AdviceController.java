package com.roommake.home.controller;

import com.roommake.home.service.HomeService;
import com.roommake.product.service.ProductService;
import com.roommake.product.vo.ProductCategory;
import com.roommake.user.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class AdviceController {

    private final ProductService productService;
    private final HomeService homeService;

    @ModelAttribute("categories")
    public List<ProductCategory> category() {
        return productService.getProductMainCategories();
    }

    // 내비바 장바구니 개수 조회
    @ModelAttribute("cartCount")
    public int cartCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            return homeService.cartCountByUserId(loginUser.getId());
        } catch (Exception e) {
            return 0;
        }
    }
}
