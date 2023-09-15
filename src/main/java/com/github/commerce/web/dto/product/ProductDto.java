package com.github.commerce.web.dto.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.commerce.entity.Order;
import com.github.commerce.entity.Product;
import com.github.commerce.entity.Seller;
import com.github.commerce.web.dto.order.DetailPageOrderDto;
import com.github.commerce.web.dto.order.OrderDto;
import com.github.commerce.web.dto.order.OrderStateEnum;
import io.swagger.models.auth.In;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long productId;

    private Long sellerId;

    private String shopName;

    private String name;

    private String content;

    private Integer price;

    private Integer leftAmount;

    private String productCategory;

    private String ageCategory;

    private String genderCategory;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String thumbnailUrl;

    private String options;

    private boolean isSeller;

    private List<DetailPageOrderDto> orderList;


    public static ProductDto fromEntity(Product product){

        return ProductDto.builder()
                .productId(product.getId())
                .sellerId(product.getSeller().getId())
                .name(product.getName())
                .content(product.getContent())
                .price(product.getPrice())
                .leftAmount(product.getLeftAmount())
                .productCategory(product.getProductCategory())
                .ageCategory(product.getAgeCategory())
                .genderCategory(product.getGenderCategory())
                .createdAt(product.getCreatedAt())
                .thumbnailUrl(product.getThumbnailUrl())
                .options(product.getOptions())
                .build();
    }


    public static ProductDto fromEntityDetail(Product product, boolean isSeller, List<DetailPageOrderDto> orderList){
        Seller seller = product.getSeller();
        return ProductDto.builder()
                .productId(product.getId())
                .sellerId(seller.getId())
                .shopName(seller.getShopName())
                .name(product.getName())
                .content(product.getContent())
                .price(product.getPrice())
                .leftAmount(product.getLeftAmount())
                .productCategory(product.getProductCategory())
                .ageCategory(product.getAgeCategory())
                .genderCategory(product.getGenderCategory())
                .createdAt(product.getCreatedAt())
                .thumbnailUrl(product.getThumbnailUrl())
                .options(product.getOptions())
                .isSeller(isSeller)
                .orderList(orderList)
                .build();
    }

    private static List<String> convertUrlList(String urlList){
        return Arrays.asList(urlList.split(","));
    }

}
