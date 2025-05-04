package com.project.shopapp.component;

import com.project.shopapp.exception.InvalidParamException;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    private final TokenRepository tokenRepository;

    @Value("${jwt.expiration}")
    private int expiration;  //save to an environment variable
    @Value("${jwt.secretKey}")
    private String secretKey;
    public String generateToken(User user) throws InvalidParamException { // method create a JWT token for user based on phoneNumber and information other(claims)
        //properties => claims
        //B1: Create a map(claims) contain information supplement(payload) for token
        Map<String, Object> claims = new HashMap<>();
        //this.generateSecretKey();

        //B2: Dat thong tin so dien thoai vao claims
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("userId", user.getId());
        try {
            //Use Jwts/builder for create token
            String token = Jwts.builder()
                    .setClaims(claims) // Dua du lieu phu (phoneNumber) vao token
                    .setSubject(user.getPhoneNumber()) // Dat Subject cua token (dung de xac dinh danh tinh cua User)
                    //Dat thoi gian het han cho token
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L)) // * 1000L convert millisecond
                    //Ky token HMAC SHA-256 voi khoa bi mat(secretKey)
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact(); // Ket thuc viec tao token
            return token;
        } catch (Exception e){
            //You can "inject" Logger, instead
            throw new InvalidParamException("Cannot create jwt token, error: " + e.getMessage());
        }
    }

    private Key getSignInKey(){
        //Decoders.BASE64.decode(r868/ekChLVjTpei1zYQeRpQyslRVxc7wUTPrePy5DA=)
        byte[] bytes = Decoders.BASE64.decode(secretKey);  //Giai ma secretKey tu chuoi Base64 thanh mang Byte
        return Keys.hmacShaKeyFor(bytes); // Tao mot khoa HMAC SHA tu mang byte
    }

    //Method dung de giai ma JWT token va trich xuat tat ca thong tin (claims)
    public Claims extractAllClaims(String token){
        return Jwts.parser() // Tao mot doi tuong JwtParser de xu ly JWT Token
                .setSigningKey(getSignInKey()) //
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String generateSecretKey(){
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        secretKey = Encoders.BASE64.encode(keyBytes);
        System.out.println(secretKey);
        return secretKey;  //  r868/ekChLVjTpei1zYQeRpQyslRVxc7wUTPrePy5DA=
    }

    public Long extractUserId(String token) {
        return extractClaims(token, claims -> Long.parseLong(claims.get("userId").toString()));
    }

    //Method giup lay bat ky thong tin nao tu Claims cua Token
    //Co the lay bat ky loai du lieu nao nho vao generic <T> va Function<Claims, T> claimsResolver
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver ){
        final Claims clams = extractAllClaims(token); // Giai ma JWT token va lay toan bo du lieu (Claims)
        return claimsResolver.apply(clams); // Lay thong tin cu the tu Claims
    }


    //Check expiration
    public boolean isTokenExpired(String token) { // Kiem tra token da het han chua
        Date expirationDate = this.extractClaims(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    public String extractPhoneNumber(String token) {
        return extractClaims(token, Claims::getSubject);
    }



    public String getSubject(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String phoneNumber = extractPhoneNumber(token);

        //Kiem tra neu token da het han
        if(isTokenExpired(token)){
            return false;
        }

        //Kiem tra neu token da bi thu hoai
        Optional<Token> storedToken = tokenRepository.findByToken(token);
        if(storedToken.isPresent() && storedToken.get().isRevoked()){
            return false;
        } else if(storedToken.isEmpty()){
            return false;
        }
        boolean result = phoneNumber.equals(userDetails.getUsername());
        return result;
    }
}
