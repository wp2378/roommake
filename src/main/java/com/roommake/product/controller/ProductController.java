package com.roommake.product.controller;

import com.roommake.admin.management.service.QnaService;
import com.roommake.admin.management.vo.Qna;
import com.roommake.admin.management.vo.QnaCategory;
import com.roommake.cart.dto.CartCreateForm;
import com.roommake.cart.dto.CartItemDto;
import com.roommake.cart.dto.CartListDto;
import com.roommake.cart.service.CartService;
import com.roommake.dto.Criteria;
import com.roommake.dto.ListDto;
import com.roommake.dto.Message;
import com.roommake.product.dto.*;
import com.roommake.product.service.ProductService;
import com.roommake.product.vo.*;
import com.roommake.resolver.Login;
import com.roommake.user.security.LoginUser;
import com.roommake.user.security.UserDetailsImpl;
import com.roommake.user.service.UserService;
import com.roommake.user.vo.ScrapFolder;
import com.roommake.user.vo.User;
import com.roommake.utils.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/store")
@RequiredArgsConstructor
@Tag(name = "Product API", description = "상품들의 조회 API를 제공한다.")
public class ProductController {

    private final ProductService productService;
    private final QnaService qnaService;
    private final CartService cartService;
    private final S3Uploader s3Uploader;
    private final UserService userService;

    // 상품홈으로 이동하는 메소드
    @GetMapping("/home")
    public String store(Model model) {

        return "store/home";
    }

    // 상품디테일로 이동하는 메소드
    @Operation(summary = "해당상품 상세 정보조회", description = "해당 상품의 정보를 조회한다")
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable int id,
                         @RequestParam(name = "page", required = false, defaultValue = "1") int CurrentPage,
                         @RequestParam(name = "rows", required = false, defaultValue = "5") int rows,
                         @RequestParam(name = "sort", required = false, defaultValue = "latest") String sort,
                         Principal principal,
                         Model model) {
        ProdctDetailCriteria prodctDetailCriteria = new ProdctDetailCriteria();
        prodctDetailCriteria.setPage(CurrentPage);
        prodctDetailCriteria.setProductId(id);
        prodctDetailCriteria.setRows(rows);
        prodctDetailCriteria.setSort(sort);

        String email = principal != null ? principal.getName() : null;

        boolean productScrapTF = productService.getProductScrapTF(id, email);
        model.addAttribute("productScrapTF", productScrapTF);

        ProductCriteria productCriteria = new ProductCriteria();
        productCriteria.setPage(CurrentPage);

        ProductDto product = productService.getProductDetailPageById(id);
        model.addAttribute("product", product);

        List<ProductDetail> productDetail = productService.getProductDetailById(id);
        model.addAttribute("productDetail", productDetail);

        ListDto<ProductReviewDto> productReviews = productService.getProductReviewsId(prodctDetailCriteria);
        model.addAttribute("reviews", productReviews.getItems());
        model.addAttribute("reviewspaging", productReviews.getPaging());

        int productReviewAmount = productService.getProductReviewAmountById(id);
        model.addAttribute("productReviewAmount", productReviewAmount);

        double productRatingTotal = productService.getProductRatingTotalById(id);
        model.addAttribute("productRatingTotal", productRatingTotal);

        List<QnaCategory> qnaCategories = qnaService.getQnaCategories();
        model.addAttribute("qnaCategories", qnaCategories);

        ListDto<ProductQnaDto> productQna = productService.getProductsQnaById(prodctDetailCriteria);
        model.addAttribute("qnas", productQna.getItems());
        model.addAttribute("qnaspaging", productQna.getPaging());

        List<ProductDto> productDifferentList = productService.getDifferentProduct(id, productCriteria);
        model.addAttribute("productDifferentList", productDifferentList);

        model.addAttribute("id", id);

        return "store/product-detail";
    }

    /**
     * 상품리스트를 불러오는 메소드
     *
     * @param model
     * @return 전체 상품리스트
     */
    @GetMapping("/category/{id}/{type}")
    public String list(@PathVariable int id,
                       @PathVariable String type,
                       @RequestParam(name = "page", required = false, defaultValue = "1") int CurrentPage,
                       @RequestParam(name = "rows", required = false, defaultValue = "12") int rows,
                       Model model) {
        ProductCriteria productCriteria = new ProductCriteria();
        productCriteria.setPage(CurrentPage);
        productCriteria.setRows(rows);

        ListDto<ProductDto> products = productService.getProductsByCategoryId(id, type, productCriteria);
        model.addAttribute("products", products.getItems());
        model.addAttribute("paging", products.getPaging());
        model.addAttribute("id", id);
        model.addAttribute("type", type);

        List<ProductCategory> productCategories = productService.getProductMainCategories();

        return "store/category-list";
    }

    @PostMapping("/addCart")
    @PreAuthorize("isAuthenticated()")
    public String addCart(@RequestParam("id") int id,
                          @RequestParam("productDetailId") List<Integer> details,
                          @RequestParam("amount") List<Integer> amounts,
                          @Login LoginUser loginUser,
                          Model model) {

        List<CartCreateForm> cartFormList = new ArrayList<>();
        for (int i = 0; i < details.size(); i++) {
            CartCreateForm form = new CartCreateForm();
            form.setProductId(id);
            form.setProductDetailId(details.get(i));
            form.setAmount(amounts.get(i));

            cartFormList.add(form);
        }
        productService.createCart(cartFormList, loginUser.getId());
        List<CartItemDto> items = cartService.getCartsByUserId(loginUser.getId());
        CartListDto dto = new CartListDto(items);

        model.addAttribute("dto", dto);

        return "cart/cart";
    }

    @GetMapping("/replyVote/{id}")
    @PreAuthorize("isAuthenticated()")
    public String replyVote(@PathVariable int id, @Login LoginUser loginuser) {

        String userNickname = loginuser.getNickname();
        productService.addProductReviewVote(id, userNickname);
        int productId = productService.getProductByreviewId(id, userNickname);

        return String.format("redirect:/store/detail/%d", productId);
    }

    @PostMapping("/qna/{id}")
    @PreAuthorize("isAuthenticated()")
    public String createQna(@PathVariable int id,
                            @RequestParam("categoryId") int categoryId,
                            @RequestParam("title") String title,
                            @RequestParam(name = "secret", required = false, defaultValue = "N") String secret,
                            @RequestParam("content") String content,
                            @Login LoginUser loginuser) {

        Product product = productService.getProductById(id);
        QnaCategory qnaCategory = qnaService.getQnaCategory(categoryId);

        Qna qna = new Qna();
        qna.setProduct(product);
        qna.setTitle(title);
        qna.setContent(content);
        qna.setCategory(qnaCategory);
        qna.setPrivateYn(secret);

        productService.createQna(qna, loginuser.getId());

        return String.format("redirect:/store/detail/%d", id);
    }

    @PostMapping("/replyCreate/{orderItemId}/{productId}")
    @PreAuthorize("isAuthenticated()")
    public String creatReply(@PathVariable int orderItemId,
                             @PathVariable int productId,
                             @RequestParam("reviewStar") int reviewStar,
                             @RequestParam("content") String content,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             @Login LoginUser loginuser) {
        ProductReviewForm productReviewForm = new ProductReviewForm();
        productReviewForm.setOrderItemId(orderItemId);
        productReviewForm.setReviewStar(reviewStar);
        productReviewForm.setContent(content);

        ProductReview productReview = productService.getProductReviewIdByuserIdorderId(orderItemId, loginuser.getId());

        if (productReview != null) {

            return "redirect:/user/myorder?error=existreview";
        } else {

            String imageName = s3Uploader.saveFile(imageFile);
            if ("default".equals(imageName)) {
                imageName = "https://roommake.s3.ap-northeast-2.amazonaws.com/3786ebc5-2ab9-4567-971d-9adfb097a153.jpg";
            }
            productService.creatReply(productReviewForm, imageName, loginuser.getId());

            return String.format("redirect:/store/detail/%d", productId);
        }
    }

    @GetMapping("/replyDelete/{reviewId}/{productId}")
    @PreAuthorize("isAuthenticated()")
    public String creatDelete(@PathVariable int reviewId,
                              @PathVariable int productId,
                              @Login LoginUser loginuser) {

        productService.deleteReply(reviewId, loginuser.getId());

        return String.format("redirect:/store/detail/%d", productId);
    }

    @GetMapping("/category")
    @ResponseBody
    public List<ProductCategory> subcategory(@RequestParam("id") int productId) {

        return productService.getProductSubCategories(productId);
    }

    @GetMapping("/qnalist")
    @ResponseBody
    public ListDto<ProductQnaDto> qnaList(@RequestParam int id,
                                          @RequestParam(name = "page", required = false, defaultValue = "1") int CurrentPage,
                                          @RequestParam(name = "rows", required = false, defaultValue = "5") int rows) {

        ProdctDetailCriteria prodctDetailCriteria = new ProdctDetailCriteria();
        prodctDetailCriteria.setPage(CurrentPage);
        prodctDetailCriteria.setProductId(id);
        prodctDetailCriteria.setRows(rows);

        ListDto<ProductQnaDto> dto = productService.getProductsQnaById(prodctDetailCriteria);
        String loginEmail = getUserEmail();

        dto.getItems().stream().forEach((qna) -> ((ProductQnaDto) qna).setLoginEmail(loginEmail));

        return dto;
    }

    @GetMapping("/reviewlist")
    @ResponseBody
    public ListDto<ProductReviewDto> reviewList(@RequestParam int id,
                                                @RequestParam(name = "page", required = false, defaultValue = "1") int CurrentPage,
                                                @RequestParam(name = "rows", required = false, defaultValue = "5") int rows,
                                                @RequestParam(name = "sort", required = false, defaultValue = "letest") String sort,
                                                @RequestParam(name = "rating", required = false, defaultValue = "0") int rating) {

        ProdctDetailCriteria prodctDetailCriteria = new ProdctDetailCriteria();
        prodctDetailCriteria.setPage(CurrentPage);
        prodctDetailCriteria.setProductId(id);
        prodctDetailCriteria.setRows(rows);
        prodctDetailCriteria.setSort(sort);

        if (rating != 0) {
            prodctDetailCriteria.setRating(rating);
        }

        ListDto<ProductReviewDto> dto = productService.getProductReviewsId(prodctDetailCriteria);
        String loginEmail = getUserEmail();
        System.out.println("로그인 이메일: " + loginEmail);
        dto.getItems().stream().forEach((review) -> ((ProductReviewDto) review).setLoginEmail(loginEmail));

        return dto;
    }

    // 스크랩 popup으로 이동하는 메소드
//    @Operation(summary = "스크랩 폴더 목록 조회", description = "모든 스크랩 폴더 목록을 조회한다.")
//    @GetMapping("/scrap/{commId}")
//    @PreAuthorize("isAuthenticated()")
//    public String scrapFolderList(@PathVariable("commId") int communityId,
//                                  @Login LoginUser loginUser, Model model) {
//
//        List<ScrapFolder> scrapFolderList = communityService.getScrapFolders(loginUser.getId());
//        model.addAttribute("scrapFolderList", scrapFolderList);
//        model.addAttribute("communityId", communityId);
//
//        return "layout/scrap-popup";
//    }

    private String getUserEmail() {
        try {
            System.out.println(SecurityContextHolder.getContext().getAuthentication());
            return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        } catch (Exception ex) {
            return null;
        }
    }
}
