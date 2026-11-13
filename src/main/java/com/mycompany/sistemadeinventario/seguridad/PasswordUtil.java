package com.mycompany.sistemadeinventario.seguridad;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash de contraseñas con PBKDF2-HMAC-SHA256 y sal aleatoria (sin dependencias externas). */
public final class PasswordUtil {
    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_CLAVE_BITS = 256;
    private static final int LONGITUD_SAL_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    public static String generarSal() {
        byte[] sal = new byte[LONGITUD_SAL_BYTES];
        RANDOM.nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal);
    }

    public static String hash(char[] clave, String salBase64) {
        PBEKeySpec spec = new PBEKeySpec(clave, Base64.getDecoder().decode(salBase64),
                ITERACIONES, LONGITUD_CLAVE_BITS);
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Base64.getEncoder().encodeToString(f.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verificar(char[] clave, String salBase64, String hashEsperado) {
        byte[] calculado = Base64.getDecoder().decode(hash(clave, salBase64));
        byte[] esperado = Base64.getDecoder().decode(hashEsperado);
        boolean ok = MessageDigest.isEqual(calculado, esperado); // comparación en tiempo constante
        Arrays.fill(calculado, (byte) 0);
        return ok;
    }
}
