package com.java.TMDTPicnic.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {
    private String label;          // "Home", "Office", ...
    private String recipientName;  // Tên người nhận
    private String phone;          // Số điện thoại
    private String province;       // Tỉnh / Thành phố
    private String district;       // Quận / Huyện
    private String ward;           // Phường / Xã
    private String detail;         // Số nhà, tên đường
    private Boolean isDefault;     // Có phải địa chỉ mặc định không
}
