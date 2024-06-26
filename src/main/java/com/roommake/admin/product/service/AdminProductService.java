package com.roommake.admin.product.service;

import com.roommake.admin.product.dto.ProductListDto;
import com.roommake.admin.product.form.ProductCreateForm;
import com.roommake.admin.product.form.ProductDetailForm;
import com.roommake.product.mapper.ProductMapper;
import com.roommake.product.vo.Product;
import com.roommake.product.vo.ProductCategory;
import com.roommake.product.vo.ProductDetail;
import com.roommake.product.vo.ProductImage;
import com.roommake.utils.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductMapper productMapper;
    private final S3Uploader s3Uploader;

    public List<Product> getAllProducts() {
        return productMapper.getAllProducts();
    }

    /**
     * @param id
     * @param model 검색기능
     */
    public void detailSearch(int id, Model model) {
        Product product = productMapper.getProductById(id);
        List<ProductImage> productImages = productMapper.getProductImages(id);
        List<ProductDetail> productDetailList = productMapper.getProductDetailById(id);
        model.addAttribute("product", product);
        model.addAttribute("defaultImage", productImages.size() > 0 ? productImages.get(0).getName() : "");
        model.addAttribute("productImages", productImages);
        model.addAttribute("productDetailList", productDetailList);
    }

    /**
     * @param form 상품등록시 이미지등록
     */
    public void insertProduct(ProductCreateForm form) {
        Product product = new Product();
        product.setName(form.getName());
        product.setPrice(form.getPrice());
        product.setDiscount(form.getDiscount());
        product.setContent(form.getContent());

        ProductCategory category = new ProductCategory();
        category.setId(form.getCategoryId() == 0 ? form.getParentCategoryId() : form.getCategoryId());
        product.setCategory(category);

        productMapper.insertProduct(product);

        ProductImage productImage = new ProductImage();
        if (!form.getImageFiles().isEmpty()) {
            for (MultipartFile multipartFile : form.getImageFiles()) {
                String filename = s3Uploader.saveFile(multipartFile);
                if ("default".equals(filename)) {
                    productImage.setProductId(product);
                    productImage.setName("https://roommake.s3.ap-northeast-2.amazonaws.com/607a6612-d8e1-427c-8d39-566fb7d06119.png");
                    productMapper.insertProductImage(productImage);
                } else {
                    productImage.setProductId(product);
                    productImage.setName(filename);
                    productMapper.insertProductImage(productImage);
                }
            }
        }
    }

    public void modifyProduct(Product product) {
        productMapper.modifyProduct(product);
    }

    /**
     * @param productDetailForm 상품 상세 등록
     * @param model
     */
    public void insertProductDetailAndSearch(ProductDetailForm productDetailForm, Model model) {
        ProductDetail productDetail = new ProductDetail();
        int productId = productDetailForm.getProductId();
        int categoryId = productMapper.getCategoryId(productId);
        productDetail.setUniqueId(categoryId * 100 + (int) Math.round(Math.random() * 100));
        productDetail.setSize(productDetailForm.getSize());
        productDetail.setColor(productDetailForm.getColor());
        productDetail.setStock(productDetailForm.getStock());
        productDetail.setProduct(productDetail.getProduct());
        productDetail.setId(productId);
        productMapper.insertProductDetail(productDetail);
        detailSearch(productId, model);
    }

    public int getTotalProducts(String keyword, String type) {
        return productMapper.getTotalProducts(keyword, type);
    }

    /**
     * @param page     상품리스트 페이징처리
     * @param pageSize
     * @param keyword
     * @param type
     * @return
     */
    public List<ProductListDto> getProductByPage(int page, int pageSize, String keyword, String type) {
        int offset = page > 0 ? (page - 1) * pageSize : 0;
        return productMapper.getProductsByPage(offset, pageSize, keyword, type);
    }

    public List<ProductCategory> getAllsubProductCategories(int categoryId) {
        return productMapper.getProductcategoriesByParentCategoryId(categoryId);
    }

    public List<ProductCategory> getAllProductCategories() {
        return productMapper.getProductCategories();
    }
}

