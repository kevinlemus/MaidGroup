package com.maidgroup.maidgroup.util.tokens;

import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.service.exceptions.InvalidTokenException;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.util.dto.PrincipalUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Log4j2
@AllArgsConstructor
@NoArgsConstructor
@Component
public class JWTUtility {

    @Value("${jwt.secret}")
    private String secret;
    private static byte[] lazySaltyBytes;
    private JWTConfig jwtConfig;

    @Autowired
    public JWTUtility(JWTConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @PostConstruct
    public void createLazySaltyBytes() {
        lazySaltyBytes = Base64.getEncoder().encode(
                Base64.getEncoder().encode(secret.getBytes())
        );
    }

    public String createToken(User user) throws IOException {
        JwtBuilder tokenBuilder = Jwts.builder()
                .setId(String.valueOf(user.getUserId()))
                .setSubject(user.getUsername())
                .setIssuer("maidgroup")
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ jwtConfig.getExpirationTime()))//seconds*minutes*1000*hours
                .signWith(Keys.hmacShaKeyFor(lazySaltyBytes), SignatureAlgorithm.HS256);
        return tokenBuilder.compact();
    }


    public boolean isTokenValid(String token){
        log.debug("Entering isTokenValid method with token: {}", token);
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            parseToken(token);
            log.debug("Token is valid");
            return true;
        } catch (MalformedJwtException | IllegalArgumentException e) {
            log.debug("An exception occurred while parsing the token", e);
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
