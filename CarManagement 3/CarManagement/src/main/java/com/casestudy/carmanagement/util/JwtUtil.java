package com.casestudy.carmanagement.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "dGhpcyBpcyBhIHZlcnkgc3BlY2lhbCBzZWNyZXQga2V5IHRoYXQgd2lsbCBiZSB1c2VkIGZvciBKU1Qgc2lnbmVkIHB1cnBvc2Vz";

    private SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token) {
        String role = Jwts.parserBuilder()
                          .setSigningKey(secretKey)
                          .build()
                          .parseClaimsJws(token)
                          .getBody()
                          .get("role", String.class);
        System.out.println("Extracted role: " + role);  // Debugging role extraction
        return (role);
    }

    public boolean validateToken(String token, String username) {
        boolean isValid = (username.equals(extractUsername(token)) && !isTokenExpired(token));
        System.out.println("Is Token Valid: " + isValid);  // Debugging token validation
        return isValid;
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        System.out.println("Token Expiration Date: " + expiration);  // Log expiration date
        return expiration.before(new Date());
    }

    private Date extractExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}
