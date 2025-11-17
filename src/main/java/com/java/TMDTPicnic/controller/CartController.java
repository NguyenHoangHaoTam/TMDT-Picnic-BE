package com.java.TMDTPicnic.controller;
import com.java.TMDTPicnic.dto.request.AddToCartRequest;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.dto.response.CartResponse;
import com.java.TMDTPicnic.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        CartResponse cart = cartService.getCartByUser(userId);
        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .message("Lấy giỏ hàng thành công")
                        .data(cart)
                        .build()
        );
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AddToCartRequest request) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub")); // Lấy userId từ token

        CartResponse updatedCart = cartService.addToCart(userId, request);

        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .message("Thêm sản phẩm vào giỏ thành công")
                        .data(updatedCart)
                        .build()
        );
    }


    @DeleteMapping("/item/{productId}")
    public ResponseEntity<ApiResponse<String>> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        cartService.removeItem(userId, productId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Xóa sản phẩm khỏi giỏ hàng thành công")
                        .data("Item removed")
                        .build()
        );
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        cartService.clearCart(userId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Xóa toàn bộ giỏ hàng thành công")
                        .data("Cart cleared")
                        .build()
        );
    }
}