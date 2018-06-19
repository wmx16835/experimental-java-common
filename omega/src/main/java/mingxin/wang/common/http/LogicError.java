package mingxin.wang.common.http;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class LogicError extends RuntimeException {
    public LogicError(String message, Throwable cause) {
        super("内部逻辑错误：" + message, cause);
    }

    public LogicError(String message) {
        super("内部逻辑错误：" + message);
    }
}
