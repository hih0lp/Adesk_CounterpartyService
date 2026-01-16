package Adesk_CounterpartyService.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(1)
public class Filter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/configuration/ui",
            "/configuration/security",
            "/favicon.ico",
            "/error"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Полностью исключаем все пути Swagger и аутентификации
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if(request.getHeader("X-Authenticated") == null || request.getHeader("X-Authenticated").isEmpty()){
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Request must come from gateway\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}