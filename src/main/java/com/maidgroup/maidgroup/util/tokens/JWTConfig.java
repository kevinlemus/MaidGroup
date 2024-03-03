package com.maidgroup.maidgroup.util.tokens;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JWTConfig {
    private Long shortExpirationTime;
    private Long longExpirationTime;

    public Long getShortExpirationTime() {
        return shortExpirationTime;
    }

    public void setShortExpirationTime(Long shortExpirationTime) {
        this.shortExpirationTime = shortExpirationTime;
    }

    public Long getLongExpirationTime() {
        return longExpirationTime;
    }

    public void setLongExpirationTime(Long longExpirationTime) {
        this.longExpirationTime = longExpirationTime;
    }
}
