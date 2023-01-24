package sweng.penelope.controllers;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class AuthUtils {
    private static final String CYPHER = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";

    public static String getKeyForIdentity(PublicKey publicKey, String identity, int campusId)
            throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        StringBuilder authBuilder = new StringBuilder();
        authBuilder.append(identity);

        if (campusId == 0)
            authBuilder.append("_admin");
        else
            authBuilder.append("_" + Integer.toString(campusId));

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/London"));
        authBuilder.append("=" + now);

        // Create cipher
        Cipher cipher = Cipher.getInstance(CYPHER);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey,
                new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));

        // Convert input to Cipher text
        byte[] encryptedKey = cipher.doFinal(authBuilder.toString().getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encryptedKey);
    }
}
