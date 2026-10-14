package spp;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    public static final int ITERATIONS = 600000;
    private PasswordUtil() { }
    public static byte[] saltBaru() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
    public static byte[] hash(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded();
        } finally { spec.clearPassword(); }
    }
    public static boolean cocok(char[] password, String salt, String stored, int iterations)
            throws GeneralSecurityException {
        byte[] calculated = hash(password, Base64.getDecoder().decode(salt), iterations);
        return MessageDigest.isEqual(calculated, Base64.getDecoder().decode(stored));
    }
}
