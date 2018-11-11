package mingxin.wang.common.json;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class IllFormedJsonPathException extends Exception {
    IllFormedJsonPathException(String path) {
        super("Trivial path \"" + path + "\" is ill-formed. Each segment if the path shall not be empty.");
    }
}
