package mingxin.wang.common.http;

import javax.servlet.http.HttpServletRequest;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class HttpUtil {
    private static final String[] PROXY_IDENTIFIERS = {
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    private HttpUtil() {
    }

    // 从请求中提取用户的真实IP
    public static String getIp(HttpServletRequest request) {
        for (String identifier : PROXY_IDENTIFIERS) {
            String result = request.getHeader(identifier);
            if (result != null && result.length() != 0 && !result.equalsIgnoreCase("unknown")) {
                return result;
            }
        }
        return request.getRemoteAddr();
    }
}
