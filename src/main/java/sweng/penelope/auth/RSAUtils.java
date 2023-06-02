package sweng.penelope.auth;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * <code>RSAUtils</code> is a utility class to handle APIKeys
 * generation/regeneration and to decrypt messages.
 */
public class RSAUtils {
    private static final int KEY_SIZE = 2048;
    private static final String ALGORITHM = "RSA";
    private static final String CYPHER = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";

    private RSAUtils() {
    }

    /**
     * Generates a {@link KeyPair}.
     * 
     * @return {@link KeyPair}
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Decrypts an encrypted message.
     * 
     * @param privateKey {@link PrivateKey} corresponding to the
     *                   {@link java.security.PublicKey PublicKey} used to encrypt.
     * @param input      The encrypted message.
     * @return {@link String} representation of the decrypted message.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static String decrypt(PrivateKey privateKey, String input) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(CYPHER);
        cipher.init(Cipher.DECRYPT_MODE, privateKey,
                new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));

        byte[] base64DecodedInput = Base64.getDecoder().decode(input);

        byte[] decryptedInput = cipher.doFinal(base64DecodedInput);

        return new String(decryptedInput, StandardCharsets.UTF_8);
    }

    public static String encrypt(PublicKey publicKey, String input) throws NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
        InvalidAlgorithmParameterException {

    Cipher cipher = Cipher.getInstance(CYPHER);
    cipher.init(Cipher.ENCRYPT_MODE, publicKey,
            new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));

    byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
    byte[] encryptedInput = cipher.doFinal(inputBytes);

    return Base64.getEncoder().encodeToString(encryptedInput);
}

}
