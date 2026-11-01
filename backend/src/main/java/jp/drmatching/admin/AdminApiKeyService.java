package jp.drmatching.admin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminApiKeyService {
    private final byte[] expectedKey;

    public AdminApiKeyService(@Value("${app.admin.api-key}") String expectedKey) {
        if (expectedKey.isBlank()) {
            throw new IllegalStateException("管理キーを空にできません。");
        }
        this.expectedKey = expectedKey.getBytes(StandardCharsets.UTF_8);
    }

    public void requireValid(String suppliedKey) {
        if (suppliedKey == null || !MessageDigest.isEqual(
                expectedKey, suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new AdminUnauthorizedException();
        }
    }
}
