package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.config.VNPayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayService {

    private final VNPayConfig vnPayConfig;

    /**
     * Tạo URL thanh toán VNPay
     * @param orderId mã đơn hàng
     * @param amount số tiền thanh toán (VND)
     * @param ipAddress IP client (lấy từ request)
     * @return URL thanh toán VNPay
     */
    public String createPaymentUrl(Long orderId, BigDecimal amount, String ipAddress) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", String.valueOf(orderId));
        params.put("vnp_OrderInfo", "Thanh toán đơn hàng #" + orderId);
        params.put("vnp_OrderType", "billpayment");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr", ipAddress);

        // Thêm ngày tạo giao dịch
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String createDate = formatter.format(new Date());
        params.put("vnp_CreateDate", createDate);

        // Thêm ngày hết hạn giao dịch (15 phút sau)
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());
        params.put("vnp_ExpireDate", expireDate);

        // Loại bỏ tham số null hoặc rỗng (nếu có)
        params.entrySet().removeIf(e -> e.getValue() == null || e.getValue().isEmpty());

        log.info("VNPay parameters: {}", params);

        String query = buildQuery(params);
        String hashData = buildHashData(params);
        String secureHash = hmacSHA512(vnPayConfig.getSecretKey(), hashData);

        log.info("VNPay hash data string: {}", hashData);
        log.info("VNPay secure hash: {}", secureHash);
        log.info("VNPay payment URL: {}?{}&vnp_SecureHash={}", vnPayConfig.getPayUrl(), query, secureHash);

        return vnPayConfig.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    // === Helper functions ===

    private String buildQuery(Map<String, String> params) {
        StringBuilder query = new StringBuilder();
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        for (int i = 0; i < fieldNames.size(); i++) {
            String name = fieldNames.get(i);
            String value = params.get(name);
            if (value != null && !value.isEmpty()) {
                query.append(URLEncoder.encode(name, StandardCharsets.US_ASCII))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (i < fieldNames.size() - 1) query.append("&");
            }
        }
        return query.toString();
    }

    private String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String name = fieldNames.get(i);
            String value = params.get(name);
            if (value != null && !value.isEmpty()) {
                sb.append(name)
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (i < fieldNames.size() - 1) sb.append("&");
            }
        }
        return sb.toString();
    }
    public boolean validateReturn(Map<String, String> vnpParams) {
        String vnp_SecureHash = vnpParams.remove("vnp_SecureHash");
        String hashData = buildHashData(vnpParams);
        String recalculatedHash = hmacSHA512(vnPayConfig.getSecretKey(), hashData);

        log.info("VNPay callback hash data: {}", hashData);
        log.info("VNPay callback provided secure hash: {}", vnp_SecureHash);
        log.info("VNPay callback recalculated secure hash: {}", recalculatedHash);

        return vnp_SecureHash != null && vnp_SecureHash.equals(recalculatedHash);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) hash.append(String.format("%02x", b));
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo HMAC SHA512", e);
        }
    }
}
