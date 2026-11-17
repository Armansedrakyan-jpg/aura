package com.example.auraclone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L;
    private static final int AUTH_LIMIT_PER_MIN = 20;
    private static final int ANALYZE_LIMIT_PER_MIN = 10;
    private static final int BATTLE_LIMIT_PER_MIN = 30;
    private static final int SHOP_LIMIT_PER_MIN = 30;
    private static final int MAX_ENTRIES = 10_000;

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        int limit = resolveLimit(path, request.getMethod());
        if (limit <= 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request) + ":" + bucketName(path);
        long now = System.currentTimeMillis();

        if (counters.size() > MAX_ENTRIES) {
            counters.entrySet().removeIf(entry -> now - entry.getValue().windowStart > WINDOW_MS * 2);
        }

        WindowCounter counter = counters.compute(clientKey, (k, existing) -> {
            if (existing == null || now - existing.windowStart >= WINDOW_MS) {
                return new WindowCounter(now, 1);
            }
            existing.count++;
            return existing;
        });

        if (counter.count > limit) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"status\":429,\"message\":\"Слишком много запросов, попробуй через минуту\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int resolveLimit(String path, String method) {
        if ("POST".equalsIgnoreCase(method) && path.startsWith("/api/auth/")) {
            return AUTH_LIMIT_PER_MIN;
        }
        if ("POST".equalsIgnoreCase(method) && path.equals("/api/aura/analyze")) {
            return ANALYZE_LIMIT_PER_MIN;
        }
        if (path.startsWith("/api/battles/")) {
            return BATTLE_LIMIT_PER_MIN;
        }
        if ("POST".equalsIgnoreCase(method) && path.startsWith("/api/shop/")) {
            return SHOP_LIMIT_PER_MIN;
        }
        return 0;
    }

    private String bucketName(String path) {
        if (path.startsWith("/api/auth/")) return "auth";
        if (path.equals("/api/aura/analyze")) return "analyze";
        if (path.startsWith("/api/shop/")) return "shop";
        return "battles";
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return "user:" + userId;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return "ip:" + forwarded.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private static final class WindowCounter {
        long windowStart;
        int count;

        WindowCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
