package com.example.demo.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
        @Value("${jwt.secret}")
        private String secretKey;

        @Value("${jwt.expiration}")
        private long expirationTime;

        public String generateToken(String email, String role) {

                return Jwts.builder()
                                .setSubject(email)
                                .claim("role", role)
                                .setIssuedAt(new Date())
                                .setExpiration(
                                                new Date(System.currentTimeMillis() + expirationTime))
                                .signWith(
                                                Keys.hmacShaKeyFor(
                                                                secretKey.getBytes(StandardCharsets.UTF_8)),
                                                SignatureAlgorithm.HS256)
                                .compact();
        }

        public Claims extractAllClaims(String token) {

                return Jwts.parserBuilder()
                                .setSigningKey(
                                                Keys.hmacShaKeyFor(
                                                                secretKey.getBytes(StandardCharsets.UTF_8)))
                                .build()
                                .parseClaimsJws(token)
                                .getBody();
        }

        public String extractEmail(String token) {
                return extractAllClaims(token).getSubject();
        }

        public String extractRole(String token) {
                return extractAllClaims(token)
                                .get("role", String.class);
        }

        public boolean isTokenValid(String token) {
                try {
                        extractAllClaims(token);
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }
}