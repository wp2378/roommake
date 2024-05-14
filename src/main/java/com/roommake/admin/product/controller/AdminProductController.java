package com.roommake.admin.product.controller;

import com.roommake.admin.product.form.ProductCreateForm;
import com.roommake.admin.product.form.ProductDetailForm;
import com.roommake.admin.product.service.AdminProductService;
import com.roommake.product.service.ProductService;
import com.roommake.product.vo.Product;
import com.roommake.product.vo.ProductCategory;
import com.roommake.utils.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("admin/product")
@RequiredArgsConstructor
public class AdminProductController {
    private final AdminProductService adminproductService;
    private final ProductService productService;
    private final S3Uploader s3Uploader;

    /**
     * @param page    상품리스트 페이징처리
     * @param size
     * @param model
     * @param keyword 상품리스트 검색
     * @param type
     * @return
     */
    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type
    ) {
        // 페이징 처리
        int pageSize = 10;
        int totalProducts = adminproductService.getTotalProducts(keyword, type);
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
        page = Math.min(Math.max(page, 1), totalPages);
        model.addAttribute("products", adminproductService.getProductByPage(page, pageSize, keyword, type));
        model.addAttribute("page", Integer.valueOf(page));
        model.addAttribute("nextPage", Integer.valueOf(page + 1));
        model.addAttribute("prevPage", Integer.valueOf(page - 1));
        model.addAttribute("totalPages", Integer.valueOf(totalPages));
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        return "admin/product/list";
    }

    /**
     * @param model 상품등록화면
     * @return
     */
    @GetMapping("/create")
    public String productform(Model model) {
        List<ProductCategory> categories = adminproductService.getAllProductCategories();
        model.addAttribute("categories", categories);
        return "admin/product/form";
    }

    @PostMapping("/create")
    public String createproduct(ProductCreateForm productCreateForm) {
        adminproductService.insertProduct(productCreateForm);

        return "redirect:/admin/product/list";
    }

    public String create(ProductCreateForm form) {
        return "redirect:/product/list";
    }

    /**
     * @param id
     * @param model 상품수정폼
     * @return
     */
    @GetMapping("/modify")
    public String modify(int id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "/admin/product/modify";
    }
    
    @PostMapping("/modify")
    public String modifyPost(Product product) {
        adminproductService.modifyProduct(product);
        return "redirect:/admin/product/list";
    }

    //상품 상세정보
    @GetMapping("/detail")
    public String detail(int id, Model model) {
        adminproductService.detailSearch(id, model);
        return "admin/product/detail";
    }

    @PostMapping("/detail")
    public String detailproduct(ProductDetailForm productDetailForm, Model model) {
        adminproductService.insertProductDetailAndSearch(productDetailForm, model);
        return "admin/product/detail";
    }

    /**
     * @param parentCategoryId 상품카테고리
     * @return
     */
    @GetMapping("/category")
    @ResponseBody
    public List<ProductCategory> categories(@RequestParam("catId") int parentCategoryId) {
        return adminproductService.getAllsubProductCategories(parentCategoryId);
    }
}