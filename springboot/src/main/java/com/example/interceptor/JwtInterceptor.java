package com.example.interceptor;

import com.example.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = getTokenFromRequest(request);
        
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"401\",\"msg\":\"未提供token\",\"data\":null}");
            return false;
        }

        try {
            // 验证token
            String username = jwtUtil.getUsernameFromToken(token);
            if (jwtUtil.validateToken(token, username)) {
                // 将用户信息存储到request中
                request.setAttribute("username", username);
                request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
                request.setAttribute("role", jwtUtil.getRoleFromToken(token));
                return true;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"401\",\"msg\":\"token无效\",\"data\":null}");
                return false;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"401\",\"msg\":\"token验证失败\",\"data\":null}");
            return false;
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
