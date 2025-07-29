package org.example.authdemo.security;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
@Getter
public class JwtKeyProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @SneakyThrows
    public JwtKeyProvider() {
        byte[] privateBytes = new ClassPathResource("private.pem").getInputStream().readAllBytes();
        byte[] publicBytes = new ClassPathResource("public.pem").getInputStream().readAllBytes();

        String privateKeyPem = new String(privateBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        String publicKeyPem = new String(publicBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decodedPrivateKey = java.util.Base64.getDecoder().decode(privateKeyPem);
        byte[] decodedPublicKey = java.util.Base64.getDecoder().decode(publicKeyPem);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedPrivateKey));
        this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedPublicKey));
    }
}
