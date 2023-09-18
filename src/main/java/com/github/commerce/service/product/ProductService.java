package com.github.commerce.service.product;

import com.github.commerce.entity.*;
import com.github.commerce.repository.order.OrderRepository;
import com.github.commerce.repository.product.ProductContentImageRepository;
import com.github.commerce.repository.product.ProductRepository;
import com.github.commerce.repository.review.ReviewRepository;
import com.github.commerce.service.product.exception.ProductErrorCode;
import com.github.commerce.service.product.exception.ProductException;
import com.github.commerce.service.product.util.ValidateProductMethod;
import com.github.commerce.web.dto.order.DetailPageOrderDto;
import com.github.commerce.web.dto.product.*;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageUploadService productImageUploadService;
    private final ValidateProductMethod validateProductMethod;
    private final OrderRepository orderRepository;
    private final ProductContentImageRepository productContentImageRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<GetProductDto> searchProducts(Integer pageNumber, String searchWord, String ageCategory, String genderCategory, String sortBy) {
        String inputAgeCategory = AgeCategoryEnum.switchCategory(ageCategory);
        String inputGenderCategory = GenderCategoryEnum.switchCategory(genderCategory);
        Pageable pageable = PageRequest.of(pageNumber - 1, 15); //한 페이지 15개
        String searchToken = "%"+searchWord+"%";

        if (Objects.equals(sortBy, "price")) {
            return productRepository.searchProductSortByPrice(searchToken, inputAgeCategory,inputGenderCategory, pageable);
           // return productList.stream().map(ProductDto::fromEntity).collect(Collectors.toList());
        }else if(Objects.equals(sortBy, "createdAt")) {
            return productRepository.searchProductSortByCreatedAt(searchToken,inputAgeCategory,inputGenderCategory, pageable);
           // return productList.stream().map(ProductDto::fromEntity).collect(Collectors.toList());
        }else {
            return productRepository.searchProductSortById(searchToken,inputAgeCategory,inputGenderCategory, pageable);
        }
    }

    //상품 등록
    @Transactional
    public ProductDto createProductItem(String productRequest,  List<MultipartFile> imageFiles, Long profileId) {
        Seller seller = validateProductMethod.validateSeller(profileId);
        boolean isSeller = validateProductMethod.isThisProductSeller(seller.getId(), profileId);

        Gson gson = new Gson();
        ProductRequest convertedRequest = gson.fromJson(productRequest, ProductRequest.class);
        List<String> options = convertedRequest.getOptions();
        String inputOptionsJson = gson.toJson(options);

        List<String> imageUrls = new ArrayList<>();

        boolean imageExists = Optional.ofNullable(imageFiles).isPresent();
        if(imageExists && imageFiles.size() > 5) throw new ProductException(ProductErrorCode.TOO_MANY_FILES);

        try{
            Product product = productRepository.save(
                    Product.builder()
                            .name(convertedRequest.getName())
                            .seller(seller)
                            .price(convertedRequest.getPrice())
                            .content(convertedRequest.getContent())
                            .leftAmount(convertedRequest.getLeftAmount())
                            .createdAt(LocalDateTime.now())
                            .isDeleted(false)
                            .productCategory(ProductCategoryEnum.switchCategory(convertedRequest.getProductCategory()))
                            .ageCategory(AgeCategoryEnum.switchCategory(convertedRequest.getAgeCategory()))
                            .genderCategory(GenderCategoryEnum.switchCategory(convertedRequest.getGenderCategory()))
                            .options(inputOptionsJson)
                            .build()
            );

            if(product.getId() != null && imageExists){
                validateProductMethod.validateImage(imageFiles);
                List<String>urlList = productImageUploadService.uploadImageFileList(imageFiles);
                imageUrls = urlList;
                for (String url : urlList) {
                        productContentImageRepository.save(ProductContentImage.from(product, url));
                    }

                    String firstUrl = urlList.get(0);
                    product.setThumbnailUrl(firstUrl);
                    return ProductDto.fromEntity(product,isSeller, imageUrls);

            }
            return ProductDto.fromEntity(product,isSeller, null);

        }catch (Exception e){
            throw new ProductException(ProductErrorCode.FAIL_TO_SAVE);
        }
    }

    @Transactional
    // 상품 삭제
    public void deleteProductByProductId(Long productId, Long profileId) {
        // 로그인한 user가 seller인지 확인
        Seller validateSeller = validateProductMethod.validateSeller(profileId);
        // 조회하려는 상품 존재하는지 확인
        Product validateProduct = validateProductMethod.validateProduct(productId);
        // 로그인한 판매자가 등록한 상품이 맞으면 삭제,아니면 예외처리
        Product existingProduct = productRepository.findBySellerIdAndId(validateSeller.getId(), validateProduct.getId());
        if (existingProduct != null) {
            productRepository.delete(existingProduct);
        } else {
            throw new ProductException(ProductErrorCode.NOT_AUTHORIZED_SELLER);
        }
    }

    @Transactional
    // 상품 수정
    public void updateProductById(Long productId, Long profileId, ProductRequest productRequest) {
        Seller validateSeller = validateProductMethod.validateSeller(profileId);
        Product validateProduct = validateProductMethod.validateProduct(productId);
        Product originProduct = productRepository.findBySellerIdAndId(validateSeller.getId(),validateProduct.getId());
        // 판매자가 등록한 상품이 아닐 경우 예외처리
        if(originProduct == null){
            throw new ProductException(ProductErrorCode.NOT_AUTHORIZED_SELLER);
        }
        try {
            Product updateProduct = Product.from(originProduct,productRequest);
            productRepository.save(updateProduct);
        } catch (Exception e){
            throw new ProductException(ProductErrorCode.UNPROCESSABLE_ENTITY);
        }
    }

    @Transactional(readOnly = true)
    public ProductDto getOneProduct(Long productId, Long userId, String userName) {
        List<String> imageUrlList = new ArrayList<>();
        Product product = productRepository.findById(productId).orElseThrow(()-> new ProductException(ProductErrorCode.NOTFOUND_PRODUCT));

        //이미지 배열 불러오기
        List<ProductContentImage> productImages = productContentImageRepository.findAllByProduct_Id(productId);
        productImages.forEach(p -> {
            imageUrlList.add(p.getImageUrl());
        });

        //채팅관련기능 : 로그인한 유저의 seller flag - true or false
        boolean isSeller = validateProductMethod.isThisProductSeller(product.getSeller().getId(), userId);

        //리뷰관련기능 : 로그인한 유저의 결제완료 주문내역
        List<Order> orderList = orderRepository.findAllByUsersIdForDetailPage(userId);
        List<DetailPageOrderDto> orderDtoList = orderList.stream().map(DetailPageOrderDto::fromEntity).collect(Collectors.toList());

        //리뷰관련기능 : 별점 평균
        Double averageStar = null;
         List<Review> reviewList = reviewRepository.findReviewsByProductId(productId, false, 0L);
        Optional<Short> optionalSum = reviewList.stream()
                .map(Review::getStarPoint)
                .reduce((s1, s2) -> (short) (s1 + s2));  // 명시적으로 Short 타입에 대한 덧셈을 수행

        if (optionalSum.isPresent() && !reviewList.isEmpty()) {
            averageStar = optionalSum.get() / (double) reviewList.size();
        }


        return ProductDto.fromEntityDetail(product, isSeller, orderDtoList, userId, userName, imageUrlList, averageStar);
    }

    @Transactional(readOnly = true)
    public List<GetProductDto> getProductsByCategory(Integer pageNumber, String productCategory, String ageCategory, String genderCategory, String sortBy) {
        String inputProductCategory = ProductCategoryEnum.switchCategory(productCategory);
        if(inputProductCategory == null) throw new ProductException(ProductErrorCode.INVALID_CATEGORY);

        Pageable pageable = PageRequest.of(pageNumber - 1, 15); //한 페이지 15개
        String inputAgeCategory = AgeCategoryEnum.switchCategory(ageCategory);
        String inputGenderCategory = GenderCategoryEnum.switchCategory(genderCategory);

        if (Objects.equals(sortBy, "createdAt")) {
            return productRepository.findByProductCategorySortByCreatedAt(inputProductCategory, inputAgeCategory, inputGenderCategory, pageable);
           // return products.stream().map(ProductDto::fromObjectResult).collect(Collectors.toList());
        }else if(Objects.equals(sortBy, "price")){
            return productRepository.findByProductCategorySortByPrice(inputProductCategory, inputAgeCategory, inputGenderCategory, pageable);
           // return products.stream().map(ProductDto::fromObjectResult).collect(Collectors.toList());
        } else{
            return productRepository.findByProductCategorySortById(inputProductCategory, inputAgeCategory, inputGenderCategory, pageable);
        }
    }

    @Transactional(readOnly = true)
    public List<GetProductDto> getProductList(Integer pageNumber, String ageCategory, String genderCategory, String sortBy) {
        Pageable pageable = PageRequest.of(pageNumber - 1, 15); //한 페이지 15개
        String inputAgeCategory = AgeCategoryEnum.switchCategory(ageCategory);
        String inputGenderCategory = GenderCategoryEnum.switchCategory(genderCategory);

        if (Objects.equals(sortBy, "price")) {
           return productRepository.findAllSortByPrice(inputAgeCategory, inputGenderCategory, pageable);
            //return products.stream().map(ProductDto::fromEntity).collect(Collectors.toList());
        } else if(Objects.equals(sortBy, "createdAt")) {
           return productRepository.findAllSortByCreatedAt(inputAgeCategory, inputGenderCategory, pageable);
            //return products.stream().map(ProductDto::fromEntity).collect(Collectors.toList());
        } else {
            return productRepository.findAllSortById(inputAgeCategory, inputGenderCategory, pageable);
        }
    }


}


