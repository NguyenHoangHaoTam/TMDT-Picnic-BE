package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.SharedCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharedCartItemRepository extends JpaRepository<SharedCartItem, Long> {
    List<SharedCartItem> findBySharedCartId(Long sharedCartId);

    Optional<SharedCartItem> findBySharedCartIdAndProductId(Long sharedCartId, Long productId);

    void deleteBySharedCartIdAndProductId(Long sharedCartId, Long productId);
}