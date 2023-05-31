package com.maidgroup.maidgroup.util.tokens;

import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.service.exceptions.InvalidTokenException;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.util.dto.PrincipalUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.IOException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
public class JWTUtility {

    @Value("${jwt.secret}")
    private String secret;
    private static byte[] lazySaltyBytes;
    @Autowired
    private JWTConfig jwtConfig;

    @PostConstruct
    public void createLazySaltyBytes() {
        lazySaltyBytes = Base64.getEncoder().encode(
                Base64.getEncoder().encode(secret.getBytes())
        );
    }

    public String createToken(User user) throws IOException {
        JwtBuilder tokenBuilder = Jwts.builder()
                .setId(String.valueOf(user.getUsername()))
                .setSubject(user.getFirstName())
                .setIssuer("maidgroup")
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ jwtConfig.getExpirationTime()))//seconds*minutes*1000*hours
                .signWith(new SecretKeySpec(lazySaltyBytes, SignatureAlgorithm.RS256.getJcaName()));

        return tokenBuilder.compact();
    }

    public boolean isTokenValid(String token){
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            parseToken(token);
            return true;
        } catch (MalformedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public PrincipalUser extractTokenDetails(String token){
        if (!isTokenValid(token)){
            throw new UnauthorizedException("You have not logged in or established a token.");
        }
        return parseToken(token).orElseThrow(() -> new InvalidTokenException("Your token is invalid or expired."));
    }

    private Optional<PrincipalUser> parseToken(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(lazySaltyBytes)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Optional.of(new PrincipalUser(
                Long.parseLong(claims.getId()),
                claims.getSubject(),
                claims.get("firstName").toString(),
                claims.get("lastName").toString(),
                claims.get("email").toString(),
                claims.get("role").toString()
        ));

    }
}
