package mingxin.wang.common.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.security.InvalidKeyException;
import java.security.Key;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
// Token管理器
@Slf4j
public final class TokenManager {

    private final Key KEY;

    public TokenManager(String tag) {
        this.KEY = AESEncryptor.generateKey(tag);
    }

    // 使用特定标识信息/来源IP和当前时间加密生成Token
    public String makeToken(String id, HttpServletRequest request) {
        try {
            return AESEncryptor.encrypt(KEY, new TokenInfo(id, HttpUtil.getIp(request), System.currentTimeMillis()));
        } catch (InvalidKeyException e) {
            log.error("内部错误，密钥验证失败", e);
            throw new LogicError("密钥格式错误", e);
        }
    }

    // 校验Token合法性/来源IP一致性和时间戳，获取标识信息
    public String verifyToken(String token, HttpServletRequest request, long expireTime) {
        try {
            TokenInfo info = AESEncryptor.decrypt(KEY, token, TokenInfo.class);
            if (HttpUtil.getIp(request).equals(info.getIp()) && System.currentTimeMillis() - info.getTimeStamp() < expireTime) {
                return info.getId();
            }
        } catch (DecryptionError e) {
            log.info("验证失败");
        } catch (InvalidKeyException e) {
            log.error("内部错误，密钥验证失败", e);
            throw new LogicError("密钥格式错误", e);
        }
        return null;
    }

    // 一个Token字符串中包含的加密信息
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static final class TokenInfo {
        private String id;
        private String ip;
        private long timeStamp;
    }
}
