package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    List<CartItem> findAllByCartId(Long cartId);
    void deleteByCartIdAndProductId(Long cartId, Long productId);
    void deleteAllByCartId(Long cartId);
}
