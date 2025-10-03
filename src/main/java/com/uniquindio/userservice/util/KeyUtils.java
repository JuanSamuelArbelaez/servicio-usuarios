package com.uniquindio.userservice.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;



@Component
@Slf4j
public class KeyUtils {

    @Getter
    private static RSAPublicKey publicKey;

    @Getter
    private static RSAPrivateKey privateKey;

    public KeyUtils() {
        initializeKeys();
    }

    private void initializeKeys() {
        try {
            String pubPath = System.getenv("PUBLIC_KEY_PATH");
            String privPath = System.getenv("PRIVATE_KEY_PATH");

// Si no están en el contenedor, usar rutas locales
            if (pubPath == null || privPath == null) {
                pubPath = "C:/Users/MI PC/Desktop/MicroServicios/Taller1_microservicios/keys/public-key.pem";
                privPath = "C:/Users/MI PC/Desktop/MicroServicios/Taller1_microservicios/keys/private-key.pem";
                System.out.println("⚠️ Variables no encontradas, usando rutas locales: " + pubPath + " y " + privPath);
            }

            Path publicKeyPath = Paths.get(pubPath);
            Path privateKeyPath = Paths.get(privPath);


            byte[] publicKeyBytes = Files.readAllBytes(publicKeyPath);
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyPath);

            publicKey = readPublicKey(publicKeyBytes);
            privateKey = readPrivateKey(privateKeyBytes);

            log.info("✅ Claves RSA cargadas exitosamente desde {} y {}",
                    publicKeyPath.toAbsolutePath(), privateKeyPath.toAbsolutePath());

        } catch (Exception e) {
            log.error("❌ Error cargando las claves RSA", e);
            throw new RuntimeException("Error cargando las keys", e);
        }
    }

    private RSAPublicKey readPublicKey(byte[] keyBytes) throws Exception {
        String publicKeyPEM = new String(keyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
    }

    private RSAPrivateKey readPrivateKey(byte[] keyBytes) throws Exception {
        String privateKeyPEM = new String(keyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }
}