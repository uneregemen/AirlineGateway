package com.ecommerce.AirlineGateway;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//@Component
public class RateLimitFilter implements Filter {

    // Her bağlanan kullanıcının IP adresine özel bir "Kova" oluşturuyoruz
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // KURAL: 1 Dakika içinde en fazla 3 isteğe izin ver (Test etmesi kolay olsun diye)
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // İsteği atan kişinin IP adresini al
        String ip = req.getRemoteAddr();
        Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());

        // Kovadan 1 hak harca
        if (bucket.tryConsume(1)) {
            // Başarılı! Hakkı var, Gateway'den geçip 8080'e gitmesine izin ver
            chain.doFilter(request, response);
        } else {
            // Başarısız! Kotası doldu (4. isteği attı). 8080'e yollamadan kapıdan çevir!
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429 Hatası
            res.getWriter().write("Too many requests! Please wait 1 minute.");
        }
    }
}