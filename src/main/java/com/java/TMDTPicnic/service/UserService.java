package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.ChangePasswordRequest;
import com.java.TMDTPicnic.dto.response.AddressResponse;
import com.java.TMDTPicnic.dto.response.UserResponse;
import com.java.TMDTPicnic.entity.Address;
import com.java.TMDTPicnic.entity.User;
import com.java.TMDTPicnic.exception.AppException;
import com.java.TMDTPicnic.exception.ErrorCode;
import com.java.TMDTPicnic.repository.AddressRepository;
import com.java.TMDTPicnic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.analysis.function.Add;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;

    public void changePassword(ChangePasswordRequest request, Principal principal) {
        // Lấy user hiện tại dựa trên email trong token
        var user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        // Update mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // Lấy thông tin người dùng theo username
    public UserResponse getUserByUsername(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }
    private UserResponse mapToUserResponse(User user) {
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        List<AddressResponse> addressResponses = addresses.stream()
                .map(addr -> AddressResponse.builder()
                        .id(addr.getId())
                        .label(addr.getLabel())
                        .recipientName(addr.getRecipientName())
                        .phone(addr.getPhone())
                        .province(addr.getProvince())
                        .district(addr.getDistrict())
                        .ward(addr.getWard())
                        .detail(addr.getDetail())
                        .isDefault(addr.getIsDefault())
                        .createdAt(addr.getCreatedAt())
                        .build())
                .toList();

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .addresses(addressResponses)
//                .avatar(user.getAvatar())
                .build();
    }
    // Lấy danh sách tất cả user (chỉ cho ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public boolean blockUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setIsActive(false);   // set flag
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);  // nếu không tìm thấy thì false
    }
    public boolean unblockUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setIsActive(true);  // bật lại user
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);  // không tìm thấy thì trả về false
    }
}
