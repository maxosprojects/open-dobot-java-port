package org.omilab.omirob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * Created by martink82cs on 16.09.2016.
 */
public class CryptoTools {
    private static final String ALGORITHM = "AES";
    private final static Logger logger = LoggerFactory.getLogger(CryptoTools.class);

    public String encrypt(final String valueEnc, final String secKey) {
        String encryptedVal = null;
        try {
            final Key key = generateKeyFromString(secKey);
            final Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, key);
            final byte[] encValue = c.doFinal(valueEnc.getBytes());
            encryptedVal = new BASE64Encoder().encode(encValue);
        } catch (Exception ex) {
            logger.warn("encrypt failed, ", ex);
        }
        return encryptedVal;
    }

    public String decrypt(final String encryptedVal, final String secretKey) {
        String decryptedValue = null;
        try {
            final Key key = generateKeyFromString(secretKey);
            final Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, key);
            final byte[] decorVal = new BASE64Decoder().decodeBuffer(encryptedVal);
            final byte[] decValue = c.doFinal(decorVal);
            decryptedValue = new String(decValue);
        } catch(Exception ex) {
            logger.warn("decrypt failed, ", ex);
        }
        return decryptedValue;
    }

    private Key generateKeyFromString(final String secKey) throws Exception {
        final byte[] keyVal = new BASE64Decoder().decodeBuffer(secKey);
        final Key key = new SecretKeySpec(keyVal, ALGORITHM);
        return key;
    }
}
