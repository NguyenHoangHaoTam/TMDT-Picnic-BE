package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.AddToCartRequest;
import com.java.TMDTPicnic.dto.response.CartItemResponse;
import com.java.TMDTPicnic.dto.response.CartResponse;
import com.java.TMDTPicnic.entity.Cart;
import com.java.TMDTPicnic.entity.CartItem;
import com.java.TMDTPicnic.entity.Product;
import com.java.TMDTPicnic.entity.User;
import com.java.TMDTPicnic.repository.CartItemRepository;
import com.java.TMDTPicnic.repository.CartRepository;
import com.java.TMDTPicnic.repository.ProductRepository;
import com.java.TMDTPicnic.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartResponse getCartByUser(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());

        return mapToCartResponse(cart, cartItems);
    }

    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> createCartItem(cart, product));

        // Cập nhật số lượng
        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        cartItemRepository.save(cartItem);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());
        return mapToCartResponse(cart, cartItems);
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteAllByCartId(cart.getId());
    }

    // ====== HÀM HỖ TRỢ ======
    private Cart createNewCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = Cart.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return cartRepository.save(cart);
    }

    private CartItem createCartItem(Cart cart, Product product) {
        return CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(0)
                .priceAtAdd(product.getPrice()) // Giá tại thời điểm thêm
                .build();
    }

    private CartResponse mapToCartResponse(Cart cart, List<CartItem> items) {
        double totalAmount = 0;
        int totalItems = 0;

        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : items) {
            double subtotal = item.getPriceAtAdd().doubleValue() * item.getQuantity();
            totalAmount += subtotal;
            totalItems += item.getQuantity();

            itemResponses.add(CartItemResponse.builder()
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .productImageUrl(item.getProduct().getImages().isEmpty() ? null : item.getProduct().getImages().get(0).getUrl())
                    .price(item.getPriceAtAdd().doubleValue())
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .build());
        }

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }
}