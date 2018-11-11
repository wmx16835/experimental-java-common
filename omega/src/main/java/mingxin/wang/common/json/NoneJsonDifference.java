package mingxin.wang.common.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class NoneJsonDifference extends StdJsonDifference {
    private final JsonNode identical;

    NoneJsonDifference(JsonNode identical) {
        super(Type.NONE);
        this.identical = identical;
    }

    @Override
    public NoneJsonDifference castToNoneDifference() {
        return this;
    }

    public JsonNode getIdentical() {
        return identical;
    }
}
