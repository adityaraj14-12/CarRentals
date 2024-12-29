package com.example.util;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenUtil {

    private static final String SECRET_KEY = "dGhpcyBpcyBhIHZlcnkgc3BlY2lhbCBzZWNyZXQga2V5IHRoYXQgd2lsbCBiZSB1c2VkIGZvciBKU1Qgc2lnbmVkIHB1cnBvc2Vz";
    private SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public Claims extractClaims(String token) {
        return Jwts.parser()
                   .setSigningKey(secretKey)
                   .parseClaimsJws(token)
                   .getBody();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractClaims(token).get("userId").toString());
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, String userEmail) {
        return userEmail.equals(extractClaims(token).getSubject()) && !isTokenExpired(token);
    }

    public String generateToken(Long userId, String userEmail) {
        Claims claims = Jwts.claims().setSubject(userEmail);
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // 1 hour validity
                .signWith(secretKey)
                .compact();
    }
}
