package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.ApplyCouponRequest;
import com.java.TMDTPicnic.dto.request.CouponCreateRequest;
import com.java.TMDTPicnic.dto.request.CouponUpdateRequest;
import com.java.TMDTPicnic.dto.response.ApplyCouponResponse;
import com.java.TMDTPicnic.dto.response.CouponCreateResponse;
import com.java.TMDTPicnic.dto.response.CouponDTOResponse;
import com.java.TMDTPicnic.dto.response.CouponPageResponse;
import com.java.TMDTPicnic.entity.Coupon;
import com.java.TMDTPicnic.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    /**
     * Admin tạo mã giảm giá mới
     */
    public CouponCreateResponse createCoupon(Long userId,CouponCreateRequest request) {
        couponRepository.findByCode(request.getCode())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã giảm giá đã tồn tại");
                });

        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode());
        coupon.setDescription(request.getDescription());
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidTo(request.getValidTo());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setIsPercent(request.getIsPercent());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setUsedCount(0);

        couponRepository.save(coupon);

        CouponCreateResponse response = new CouponCreateResponse();
        response.setId(coupon.getId());
        response.setCode(coupon.getCode());
        response.setMessage("Tạo mã giảm giá thành công");
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    /**
     * Người dùng áp dụng mã giảm giá khi thanh toán
     */
    public ApplyCouponResponse applyCoupon(ApplyCouponRequest request) {
        Optional<Coupon> optionalCoupon = couponRepository.findByCode(request.getCode());
        ApplyCouponResponse response = new ApplyCouponResponse();
        response.setCode(request.getCode());

        if (optionalCoupon.isEmpty()) {
            response.setValid(false);
            response.setMessage("Mã giảm giá không tồn tại");
            return response;
        }

        Coupon coupon = optionalCoupon.get();

        // Kiểm tra hiệu lực
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidTo())) {
            response.setValid(false);
            response.setMessage("Mã giảm giá đã hết hạn hoặc chưa có hiệu lực");
            return response;
        }

        // Kiểm tra số lượt sử dụng
        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            response.setValid(false);
            response.setMessage("Mã giảm giá đã hết lượt sử dụng");
            return response;
        }

        // Tính toán giảm giá
        BigDecimal discountAmount;
        if (coupon.getIsPercent()) {
            discountAmount = request.getOrderTotal()
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
        } else {
            discountAmount = coupon.getDiscountValue();
        }

        // Tổng sau khi giảm
        BigDecimal finalTotal = request.getOrderTotal().subtract(discountAmount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0)
            finalTotal = BigDecimal.ZERO;

        // Cập nhật lượt sử dụng
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        // Trả kết quả
        response.setValid(true);
        response.setMessage("Áp dụng mã giảm giá thành công");
        response.setDiscountAmount(discountAmount);
        response.setFinalTotal(finalTotal);

        return response;
    }

    public CouponDTOResponse getCouponInfo(String code) {
        Optional<Coupon> optional = couponRepository.findByCode(code);
        if (optional.isEmpty()) return null;
        return mapToDto(optional.get());
    }

    public CouponPageResponse getCoupons(Pageable pageable, String search, String status) {
        String keyword = (search == null || search.isBlank()) ? null : search.trim();
        String normalizedStatus = (status == null || status.isBlank()) ? null : status.trim().toUpperCase();

        Page<Coupon> couponPage = couponRepository.searchCoupons(keyword, normalizedStatus, LocalDateTime.now(), pageable);

        return new CouponPageResponse(
                couponPage.getContent()
                        .stream()
                        .map(this::mapToDto)
                        .collect(Collectors.toList()),
                couponPage.getNumber(),
                couponPage.getSize(),
                couponPage.getTotalElements(),
                couponPage.getTotalPages(),
                couponPage.isFirst(),
                couponPage.isLast()
        );
    }

    public CouponDTOResponse updateCoupon(Long id, CouponUpdateRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã giảm giá"));

        if (!coupon.getCode().equalsIgnoreCase(request.getCode())) {
            couponRepository.findByCode(request.getCode())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Mã giảm giá đã tồn tại");
                    });
        }

        coupon.setCode(request.getCode());
        coupon.setDescription(request.getDescription());
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidTo(request.getValidTo());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setIsPercent(request.getIsPercent());
        coupon.setUsageLimit(request.getUsageLimit());

        couponRepository.save(coupon);
        return mapToDto(coupon);
    }

    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã giảm giá"));
        couponRepository.delete(coupon);
    }

    private CouponDTOResponse mapToDto(Coupon coupon) {
        CouponDTOResponse dto = new CouponDTOResponse();
        dto.setId(coupon.getId());
        dto.setCode(coupon.getCode());
        dto.setDescription(coupon.getDescription());
        dto.setValidFrom(coupon.getValidFrom());
        dto.setValidTo(coupon.getValidTo());
        dto.setDiscountValue(coupon.getDiscountValue());
        dto.setIsPercent(coupon.getIsPercent());
        dto.setUsageLimit(coupon.getUsageLimit());
        dto.setUsedCount(coupon.getUsedCount());
        return dto;
    }
}
