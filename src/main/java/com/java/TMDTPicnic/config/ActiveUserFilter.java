package com.java.TMDTPicnic.config;


import com.java.TMDTPicnic.entity.User;
import com.java.TMDTPicnic.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ActiveUserFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public ActiveUserFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Long userId = Long.valueOf(authentication.getName());
            User user = userRepository.findById(userId).orElse(null);
            System.out.println("Authenticated user: " + authentication.getName());
            if (user == null || Boolean.FALSE.equals(user.getIsActive())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Tài khoản đã bị vô hiệu hóa.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
