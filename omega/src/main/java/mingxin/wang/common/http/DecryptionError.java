package mingxin.wang.common.http;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
// 解密异常
public class DecryptionError extends Exception {
    public DecryptionError(Throwable cause) {
        super("解密失败，密文与密钥不匹配", cause);
    }
}
