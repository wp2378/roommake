package com.roommake.product.mapper;

import com.roommake.admin.management.vo.Qna;
import com.roommake.admin.product.dto.ProductListDto;
import com.roommake.cart.vo.Cart;
import com.roommake.dto.Criteria;
import com.roommake.order.vo.Order;
import com.roommake.order.vo.OrderItem;
import com.roommake.product.dto.*;
import com.roommake.product.vo.*;
import com.roommake.user.vo.ProductScrap;
import com.roommake.user.vo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface ProductMapper {

    List<Product> getAllProducts();

    List<ProductDto> getProductsByParentsId(ProductCriteria productCriteria);

    List<ProductDto> getProductsBySubCategoryId(ProductCriteria productCriteria);

    List<ProductTag> getAllProductTags();

    ProductDto getProductDetailPageById(int id);

    Product getProductById(int id);

    List<ProductDetail> getProductDetailById(int id);

    List<ProductCategory> getProductMainCategories();

    List<ProductCategory> getProductSubCategories(int id);

    void createCart(Cart cart);

    void insertProduct(Product product);

    void insertProductImage(ProductImage productImage);

    List<ProductListDto> getProducts();

    void modifyProduct(Product product);

    List<ProductImage> getProductImages(int productId);

    int getCategoryId(@Param("id") int productId);

    void insertProductDetail(ProductDetail productDetail);

    List<ProductReviewDto> getProductReviewsByProductId(ProdctDetailCriteria prodctDetailCriteria);

    int getProductReviewAmountById(int productId);

    int getProductRatingTotalById(int productId);

    List<ProductListDto> getProductsByPage(@Param("offset") int offset, @Param("pageSize") int pageSize, @Param("keyword") String keyword, @Param("type") String type);

    int getTotalProducts(@Param("keyword") String keyword, @Param("type") String type);

    void addProductReviewVote(ProductReviewVote productReviewVote);

    ProductReviewVote getProductReviewVoteById(ProductReviewVote productReviewVote);

    ProductReview getProductReviewById(int reviewId);

    void deleteProductReviewVoteById(ProductReviewVote productReviewVote);

    void addCountProductReviewVote(int reviewId);

    void deleteCountProductReviewVote(int reviewId);

    void createQna(Qna qna);

    List<ProductQnaDto> getProductQnasById(int id);

    int getProductByreviewId(ProductReviewVote productReviewVote);

    int getTotalQnaCountByProdId(int id);

    List<ProductQnaDto> getProductQnas(ProdctDetailCriteria prodctDetailCriteria);

    List<ProductDto> getDifferentProduct(ProductCriteria productCriteria);

    int getProductCategoryIdByProductId(int productId);

    List<ProductTagCategory> getTagsByProductId(int productId);

    double getProductRatingByProductId(int productId);

    int getTotalProductsCountByParentsCategoryId(int categoryId);

    int getTotalProductsCountBySubCategoryId(int categoryId);

    List<ProductCategory> getProductCategories();

    List<ProductCategory> getProductcategoriesByParentCategoryId(int categoryId);

    ProductCategory getProductCategoryById(int id);

    int getTotalReviewCountByProdId(int productId);

    OrderItem getOrderItemById(int orderItemId);

    void createProductReview(ProductReview productReview);

    void deleteProductReview(ProductReview productReview);

    void createPlusPointHistory(@Param("amount") int amount,
                                @Param("userId") int userId,
                                @Param("typeId") int typeId,
                                @Param("pointReason") String pointReason);

    ProductReview getProductReviewIdByuserIdorderId(@Param("orderItemId") int orderItemId,
                                                    @Param("userId") int userId);

    void addPlusPoint(@Param("userId") int userId,
                      @Param("point") int point);

    String getProductReviewImageName(ProductReview productReview);

    void addMinusPoint(@Param("userId") int userId,
                       @Param("point") int point);

    ProductScrap getProductScrapTF(User user, Product product);
}