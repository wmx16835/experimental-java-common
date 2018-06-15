package mingxin.wang.common.http;

import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
@Slf4j
public final class LoginCookieManager {

    // 身份过期时间
    private final int COOKIE_EXPIRE_SEC;

    // Cookie中Token的标识符
    private final String COOKIE_NAME;

    // Token管理器
    private final TokenManager TOKEN_MANAGER;

    public LoginCookieManager(String cookieName, int cookieExpireSec) {
        this.COOKIE_NAME = cookieName;
        this.COOKIE_EXPIRE_SEC = cookieExpireSec;
        this.TOKEN_MANAGER = new TokenManager(cookieName + cookieExpireSec);
    }

    // 设置身份信息
    public void setLoginStatus(String id, HttpServletRequest request, HttpServletResponse response) {
        // 生成Token信息
        String token = TOKEN_MANAGER.makeToken(id, request);

        // 生成cookie并写入应答
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    // 清除身份信息
    public void clearLoginStatus(HttpServletResponse response) {
        // 写入Token
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    // 获取身份信息
    public Optional<String> getLoginStatus(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.absent();
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(COOKIE_NAME)) {
                String id = TOKEN_MANAGER.verifyToken(cookie.getValue(), request, COOKIE_EXPIRE_SEC * 1000L);
                if (id == null) {
                    return Optional.absent();
                }
                setLoginStatus(id, request, response);
                return Optional.of(id);
            }
        }
        return Optional.absent();
    }
}
