package yolohome.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** Secret key dung de ky JWT - PHAI duoc set qua bien moi truong khi deploy, khong dung gia tri mac dinh cho production. */
    private String secret;

    /** Thoi gian song cua token, tinh bang phut. */
    private long expirationMinutes = 60 * 24 * 7; // mac dinh 7 ngay
}
