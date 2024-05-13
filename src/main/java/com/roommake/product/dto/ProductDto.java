package com.roommake.product.dto;

import com.roommake.product.vo.ProductCategory;
import com.roommake.product.vo.ProductTagCategory;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {

    private int id;
    private String name;
    private String content;
    private String statusYn;
    private Date createDate;
    private Date updateDate;
    private Date deleteDate;
    private String deleteYn;
    private double price;
    private double discount;
    private int categoryId;
    private int parentsCategoryId;
    private int parentsId;
    private String imageName;
    private ProductCategory category;
    private List<ProductTagCategory> tags;
    private double productRating;
    private int productScrapCount;           // 상품스크랩수
}
