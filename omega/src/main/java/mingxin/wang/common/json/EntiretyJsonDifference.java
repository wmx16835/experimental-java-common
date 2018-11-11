package mingxin.wang.common.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class EntiretyJsonDifference extends StdJsonDifference {
    private final JsonNode left;
    private final JsonNode right;

    EntiretyJsonDifference(JsonNode left, JsonNode right) {
        super(Type.ENTIRETY);
        this.left = left;
        this.right = right;
    }

    @Override
    public EntiretyJsonDifference castToEntiretyDifference() {
        return this;
    }

    public JsonNode getLeft() {
        return left;
    }

    public JsonNode getRight() {
        return right;
    }
}
