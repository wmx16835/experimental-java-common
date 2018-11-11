package mingxin.wang.common.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
// 封装Java提供的AES加密算法
@Slf4j
public final class AESEncryptor {
    // 加密算法标识
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final MessageDigest MESSAGE_DIGEST;

    static {
        try {
            MESSAGE_DIGEST = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("内部错误，请检查安全模块配置", e);
            throw new LogicError("安全模块配置错误", e);
        }
    }

    // 使用key对data加密，data必须是数据对象，输出Base64序列化的密文
    public static String encrypt(Key key, Object data) throws InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = Jsons.getDefaultObjectMapper().writeValueAsBytes(data);
            return Base64.encodeBase64String(cipher.doFinal(encrypted));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            log.error("内部错误，请检查加密模块配置", e);
            throw new LogicError("加密模块配置错误", e);
        } catch (JsonProcessingException e) {
            log.error("内部错误，请检查序列化模块配置", e);
            throw new LogicError("序列化模块配置错误", e);
        }
    }

    // 使用key对密文进行解密
    public static <T> T decrypt(Key key, String data, Class<T> clazz) throws InvalidKeyException, DecryptionError {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.decodeBase64(data));
            return Jsons.getDefaultObjectMapper().readValue(decrypted, clazz);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.error("内部错误，请检查加密模块配置", e);
            throw new LogicError("加密模块配置错误", e);
        } catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new DecryptionError(e);
        }
    }

    // 根据tag生成key
    public static Key generateKey(String tag) {
        byte[] digest = MESSAGE_DIGEST.digest(tag.getBytes());
        return new SecretKeySpec(Arrays.copyOfRange(digest, 8, 24), "AES");
    }
}
