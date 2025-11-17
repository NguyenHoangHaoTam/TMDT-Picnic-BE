package com.java.TMDTPicnic.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.java.TMDTPicnic.dto.request.ProductRequest;
import com.java.TMDTPicnic.dto.response.ProductImageResponse;
import com.java.TMDTPicnic.dto.response.ProductResponse;
import com.java.TMDTPicnic.entity.Category;
import com.java.TMDTPicnic.entity.Product;
import com.java.TMDTPicnic.entity.ProductImage;
import com.java.TMDTPicnic.repository.CategoryRepository;
import com.java.TMDTPicnic.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Cloudinary cloudinary;

    // ==============================
    // CREATE PRODUCT
    // ==============================
    public ProductResponse createProduct(ProductRequest request, List<MultipartFile> imageFiles) {
        Product product = new Product();
        applyProductFields(product, request);

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdateAt(LocalDateTime.now());

        // Gán category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Upload ảnh nếu có
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<ProductImage> images = uploadImages(imageFiles, product);
            product.setImages(images);
        }

        productRepository.save(product);
        return mapToResponse(product);
    }

    // ==============================
    // UPDATE PRODUCT
    // ==============================
    public ProductResponse updateProduct(Long id, ProductRequest request, List<MultipartFile> imageFiles) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        applyProductFields(product, request);
        product.setUpdateAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Thêm ảnh mới nếu có
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<ProductImage> newImages = uploadImages(imageFiles, product);
            product.getImages().addAll(newImages);
        }

        productRepository.save(product);
        return mapToResponse(product);
    }

    // ==============================
    // HIDE / UNHIDE PRODUCT
    // ==============================
    public ProductResponse hideProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsActive(false);
        product.setUpdateAt(LocalDateTime.now());
        productRepository.save(product);
        return mapToResponse(product);
    }

    public ProductResponse unhideProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsActive(true);
        product.setUpdateAt(LocalDateTime.now());
        productRepository.save(product);
        return mapToResponse(product);
    }

    // ==============================
    // GET METHODS
    // ==============================
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found with slug: " + slug));
        return mapToResponse(product);
    }

    public Page<ProductResponse> getProductsByCategoryId(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);

        if (products.isEmpty()) {
            throw new RuntimeException("No products found for category ID: " + categoryId);
        }

        return products.map(this::mapToResponse);
    }

    public Page<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    // ==============================
    // FILTER PRODUCTS
    // ==============================
    public List<ProductResponse> getProductsByFilter(String filter) {
        switch (filter.toLowerCase()) {
            case "featured":
                return productRepository.findByIsFeaturedTrue().stream()
                        .limit(10).map(this::mapToResponse).toList();

            case "newest":
                return productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                        .limit(10).map(this::mapToResponse).toList();

            case "discount":
                return productRepository.findByDiscountRateGreaterThan(BigDecimal.ZERO).stream()
                        .sorted((a, b) -> b.getDiscountRate().compareTo(a.getDiscountRate()))
                        .limit(10).map(this::mapToResponse).toList();

            case "best-seller":
                return productRepository.findAll(Sort.by(Sort.Direction.DESC, "soldQuantity")).stream()
                        .limit(10).map(this::mapToResponse).toList();

            default:
                throw new IllegalArgumentException("Invalid filter type: " + filter);
        }
    }

    // ==============================
    // DELETE PRODUCT
    // ==============================
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    // ==============================
    // HELPER METHODS
    // ==============================
    private void applyProductFields(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setUnit(request.getUnit());
        product.setIsActive(request.getIsActive());
        product.setIsFeatured(request.getIsFeatured());
        product.setDiscountRate(request.getDiscountRate());
    }

    private List<ProductImage> uploadImages(List<MultipartFile> files, Product product) {
        List<ProductImage> images = new ArrayList<>();
        for (MultipartFile file : files) {
            images.add(uploadImageToCloudinary(file, product));
        }
        return images;
    }

    private ProductImage uploadImageToCloudinary(MultipartFile file, Product product) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "products"));

            ProductImage image = new ProductImage();
            image.setUrl(uploadResult.get("secure_url").toString());
            image.setAltText(product.getName());
            image.setProduct(product);
            return image;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .discountRate(product.getDiscountRate())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .unit(product.getUnit())
                .soldQuantity(product.getSoldQuantity())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .isFeatured(product.getIsFeatured())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .images(product.getImages() != null
                        ? product.getImages().stream()
                        .map(img -> ProductImageResponse.builder()
                                .id(img.getId())
                                .url(img.getUrl())
                                .altText(img.getAltText())
                                .build())
                        .collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }
}
