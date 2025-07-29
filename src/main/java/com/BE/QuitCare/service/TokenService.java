package com.BE.QuitCare.service;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.repository.AuthenticationRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class TokenService {

    @Autowired
    AuthenticationRepository authenticationRepository;

    private final String SECRET_KEY = "4bb6d1dfbafb64a681139d1586b6f1160d18159afd57c8c79136d7490630407c";

    private SecretKey getSigninKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Account account) {// nhận vào account và generate nó để nhận account đó
        String token =
                // create object of JWT
                Jwts.builder().
                        //subject of token
                                subject(account.getEmail()).
                        // time Create Token
                                issuedAt(new Date(System.currentTimeMillis()))
                        // Time exprire of Token
                        .expiration(new Date(System.currentTimeMillis()+24*60*60*1000))
                        //
                        .signWith(getSigninKey())
                        .compact();
        return token;
    }

    // form token to Claim Object
    public Claims extractAllClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT token is null or empty");
        }
        return  Jwts.parser().
                verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // get userName form CLAIM
    public Account extractAccount (String token){
        String email = extractClaim(token,Claims::getSubject);
        return authenticationRepository.findAccountByEmail(email);
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token); // sẽ throw nếu token không hợp lệ
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    public String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        return extractAccount(token).getId(); // không cần viết riêng nếu có extractAccount
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token); // sẽ throw nếu token không hợp lệ
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    public String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        return extractAccount(token).getId(); // không cần viết riêng nếu có extractAccount
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
    // get Expiration form CLAIM
    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }

    // from claim and extract specific data type.
    public <T> T extractClaim(String token, Function<Claims,T> resolver){
        Claims claims = extractAllClaims(token);
        return  resolver.apply(claims);
    }
}

