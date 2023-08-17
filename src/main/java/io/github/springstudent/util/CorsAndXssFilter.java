package io.github.springstudent.util;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 跨域和xss控制过滤器
 *
 * @author zhouning
 * @date 2022/07/22 13:39
 */
@WebFilter("/**")
@Component
public class CorsAndXssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.addHeader("Access-Control-Allow-Methods", "*");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type,Cookie, Token, Random, Signature, Timestamp, Content-Length,attachment, Content-Disposition, filename,Accept-Ranges,Content-Range");
        response.setHeader("Access-Control-Max-Age", "3600");
        filterChain.doFilter(request, response);
    }

}