package com.roommake.product.service;

import com.roommake.admin.management.vo.Qna;
import com.roommake.admin.product.dto.ProductListDto;
import com.roommake.admin.product.form.ProductCreateForm;
import com.roommake.cart.dto.CartCreateForm;
import com.roommake.cart.vo.Cart;
import com.roommake.community.vo.Community;
import com.roommake.community.vo.CommunityLike;
import com.roommake.community.vo.CommunityScrap;
import com.roommake.dto.ListDto;
import com.roommake.dto.Pagination;
import com.roommake.order.mapper.OrderMapper;
import com.roommake.order.vo.Order;
import com.roommake.order.vo.OrderItem;
import com.roommake.product.dto.*;
import com.roommake.product.mapper.ProductMapper;
import com.roommake.product.vo.*;
import com.roommake.user.enums.PointReasonEnum;
import com.roommake.user.mapper.UserMapper;
import com.roommake.user.vo.Follow;
import com.roommake.user.vo.ProductScrap;
import com.roommake.user.vo.User;
import com.roommake.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class ProductService {

    private String saveDirectory = "C:\\roommake\\src\\main\\resources\\static\\images\\product";

    @Autowired
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    /**
     * 모든상품 리스트를 반환한다.
     *
     * @return 모든상품리스트
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productMapper.getAllProducts();
    }

    @Transactional(readOnly = true)
    public ListDto<ProductDto> getProductsByCategoryId(int categoryId, String type, ProductCriteria productCriteria) {

        productCriteria.setProdCategoryId(categoryId);

        int totalProductCount = 0;
        if ("top".equals(type)) {
            totalProductCount = productMapper.getTotalProductsCountByParentsCategoryId(categoryId);
        } else {
            totalProductCount = productMapper.getTotalProductsCountBySubCategoryId(categoryId);
        }

        Pagination pagination = new Pagination(productCriteria.getPage(), totalProductCount, productCriteria.getRows());
        productCriteria.setBegin(pagination.getBegin());
        productCriteria.setEnd(pagination.getEnd());

        List<ProductDto> productList = List.of();
        if ("top".equals(type)) {
            productList = productMapper.getProductsByParentsId(productCriteria);
        } else {
            productList = productMapper.getProductsBySubCategoryId(productCriteria);
        }
        return new ListDto<ProductDto>(productList, pagination);
    }

    @Transactional(readOnly = true)
    public List<ProductTag> getAllProductTags() {
        return productMapper.getAllProductTags();
    }

    @Transactional(readOnly = true)
    public ProductDto getProductDetailPageById(int id) {
        return productMapper.getProductDetailPageById(id);
    }

    @Transactional(readOnly = true)
    public Product getProductById(int id) {
        return productMapper.getProductById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductDetail> getProductDetailById(int id) {
        return productMapper.getProductDetailById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductCategory> getProductMainCategories() {
        return productMapper.getProductMainCategories();
    }

    @Transactional(readOnly = true)
    public List<ProductCategory> getProductSubCategories(int id) {
        return productMapper.getProductSubCategories(id);
    }

    public void createCart(List<CartCreateForm> formList, int userId) {

        for (CartCreateForm x : formList) {
            User user = User.builder().id(userId).build();

            Product product = new Product();
            product.setId(x.getProductId());

            ProductDetail productDetail = new ProductDetail();
            productDetail.setId(x.getProductDetailId());

            Cart cart = new Cart();

            cart.setProduct(product);
            cart.setUser(user);
            cart.setProductDetail(productDetail);
            cart.setAmount(x.getAmount());

            productMapper.createCart(cart);
        }
    }

    public void insertProduct(ProductCreateForm form) {
        Product product = new Product();
        product.setName(form.getName());
        product.setPrice(form.getPrice());
        product.setDiscount(form.getDiscount());
        product.setContent(form.getContent());
        ProductCategory category = new ProductCategory();
        category.setId(form.getCategoryId());
        product.setCategory(category);
        productMapper.insertProduct(product);

        for (MultipartFile mf : form.getImageFiles()) {
            String filename = FileUtils.upload(mf, saveDirectory);
            ProductImage productImage = new ProductImage();
            productImage.setProductId(product);
            productImage.setName(filename);
            productMapper.insertProductImage(productImage);
        }
    }

    @Transactional(readOnly = true)
    public List<ProductListDto> getProducts() {
        return productMapper.getProducts();
    }

    @Transactional(readOnly = true)
    public List<ProductImage> getProductImagesById(int id) {
        return productMapper.getProductImages(id);
    }

    @Transactional(readOnly = true)
    public ListDto<ProductReviewDto> getProductReviewsId(ProdctDetailCriteria prodctDetailCriteria) {

        int totatalReviewCount = productMapper.getTotalReviewCountByProdId(prodctDetailCriteria.getProductId());

        Pagination pagination = new Pagination(prodctDetailCriteria.getPage(), totatalReviewCount, prodctDetailCriteria.getRows());

        prodctDetailCriteria.setBegin(pagination.getBegin());
        prodctDetailCriteria.setEnd(pagination.getEnd());

        List<ProductReviewDto> reviewList = productMapper.getProductReviewsByProductId(prodctDetailCriteria);
        System.out.println(reviewList);

        ListDto<ProductReviewDto> dto = new ListDto<ProductReviewDto>(reviewList, pagination);
        return dto;
    }

    @Transactional(readOnly = true)
    public int getProductReviewAmountById(int id) {

        return productMapper.getProductReviewAmountById(id);
    }

    // 상품 리뷰평균점수를 확인하는 구문
    @Transactional(readOnly = true)
    public int getProductRatingTotalById(int productId) {

        return productMapper.getProductRatingTotalById(productId);
    }

    public void addProductReviewVote(int reviewId, String userNickname) {
        ProductReview productReview = productMapper.getProductReviewById(reviewId);
        User user = userMapper.getUserByNickname(userNickname);

        ProductReviewVote productReviewVote = new ProductReviewVote();
        productReviewVote.setUser(user);
        productReviewVote.setReview(productReview);

        ProductReviewVote saveData = productMapper.getProductReviewVoteById(productReviewVote);

        if (saveData == null) {
            productMapper.addProductReviewVote(productReviewVote);
            productMapper.addCountProductReviewVote(reviewId);
        } else {
            productMapper.deleteProductReviewVoteById(productReviewVote);
            productMapper.deleteCountProductReviewVote(reviewId);
        }
    }

    @Transactional(readOnly = true)
    public ProductReview getProductReviewById(int id) {

        return productMapper.getProductReviewById(id);
    }

    public void createQna(Qna qna, int userId) {

        User user = User.builder().id(userId).build();

        qna.setUser(user);

        productMapper.createQna(qna);
    }

//    public List<ProductQnaDto> getProductQnasById(int id) {
//
//        return productMapper.getProductQnasById(id);
//    }

    @Transactional(readOnly = true)
    public int getProductByreviewId(int reviewId, String userNickname) {
        ProductReview productReview = productMapper.getProductReviewById(reviewId);
        User user = userMapper.getUserByNickname(userNickname);

        ProductReviewVote productReviewVote = new ProductReviewVote();
        productReviewVote.setUser(user);
        productReviewVote.setReview(productReview);

        return productMapper.getProductByreviewId(productReviewVote);
    }

    @Transactional(readOnly = true)
    public ListDto<ProductQnaDto> getProductsQnaById(ProdctDetailCriteria prodctDetailCriteria) {

        int totalQnaCount = productMapper.getTotalQnaCountByProdId(prodctDetailCriteria.getProductId());

        Pagination pagination = new Pagination(prodctDetailCriteria.getPage(), totalQnaCount, prodctDetailCriteria.getRows());

        prodctDetailCriteria.setBegin(pagination.getBegin());
        prodctDetailCriteria.setEnd(pagination.getEnd());

        List<ProductQnaDto> qnaList = productMapper.getProductQnas(prodctDetailCriteria);

        ListDto<ProductQnaDto> dto = new ListDto<ProductQnaDto>(qnaList, pagination);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getDifferentProduct(int productId, ProductCriteria productCriteria) {

        int categoryId = productMapper.getProductCategoryIdByProductId(productId);

        productCriteria.setProdCategoryId(categoryId);
        productCriteria.setRows(4);
        productCriteria.setBegin(1);
        productCriteria.setEnd(4);

        List<ProductDto> prodList = productMapper.getDifferentProduct(productCriteria);

        return prodList;
    }

    public void creatReply(ProductReviewForm productReviewForm, String imageName, int userId) {
        User user = User.builder().id(userId).build();

        int orderitemid = productReviewForm.getOrderItemId();
        OrderItem orderItem = productMapper.getOrderItemById(orderitemid);

        ProductReview productReview = new ProductReview();
        productReview.setUser(user);
        productReview.setContent(productReviewForm.getContent());
        productReview.setOrderItem(orderItem);
        productReview.setRating(productReviewForm.getReviewStar());
        productReview.setProductReviewImage(imageName);

        productMapper.createProductReview(productReview);

        if (imageName.equals("https://roommake.s3.ap-northeast-2.amazonaws.com/3786ebc5-2ab9-4567-971d-9adfb097a153.jpg")) {
            String reason = PointReasonEnum.NORMAL_REVIEW_WRITE.getReason(); // 적립 상세사유 고정문구
            int point = 100;

            productMapper.addPlusPoint(userId, point);
            productMapper.createPlusPointHistory(point, userId, 7, reason);
        } else {
            String reason = PointReasonEnum.PHOTO_REVIEW_WRITE.getReason(); // 적립 상세사유 고정문구
            int point = 500;

            productMapper.addPlusPoint(userId, point);
            productMapper.createPlusPointHistory(point, userId, 7, reason);
        }
    }

    public void deleteReply(int reviewId, int userId) {
        User user = User.builder().id(userId).build();

        ProductReview productReview = new ProductReview();
        productReview.setId(reviewId);
        productReview.setUser(user);

        ProductReview productReview1 = productMapper.getProductReviewById(reviewId);

        if (productReview1.getProductReviewImage().equals("https://roommake.s3.ap-northeast-2.amazonaws.com/3786ebc5-2ab9-4567-971d-9adfb097a153.jpg")) {
            int point = 100;

            productMapper.addMinusPoint(userId, point);
        } else {
            int point = 500;

            productMapper.addMinusPoint(userId, point);
        }

        productMapper.deleteProductReview(productReview);
    }

    @Transactional(readOnly = true)
    public ProductReview getProductReviewIdByuserIdorderId(int orderItemId, int userId) {

        return productMapper.getProductReviewIdByuserIdorderId(orderItemId, userId);
    }

    public boolean getProductScrapTF(int productId, String email) {
        Product product = productMapper.getProductById(productId);
        User user = userMapper.getUserByEmail(email);

        ProductScrap productScrap = productMapper.getProductScrapTF(user, product);

        if (productScrap != null) {
            return true;
        } else {
            return false;
        }
    }
}
