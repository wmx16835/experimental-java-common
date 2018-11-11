package mingxin.wang.common.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
final class ArrayJsonDifferenceIgnoreIdentical extends JsonDifference {
    private final List<JsonNode> leftOnly;
    private final List<JsonNode> rightOnly;
    private final int identicalCount;

    ArrayJsonDifferenceIgnoreIdentical(ArrayJsonDifference original) {
        super(Type.ARRAY);
        this.leftOnly = original.getLeftOnly();
        this.rightOnly = original.getRightOnly();
        this.identicalCount = original.getIdentical().size();
    }

    public List<JsonNode> getLeftOnly() {
        return leftOnly;
    }

    public List<JsonNode> getRightOnly() {
        return rightOnly;
    }

    public int getIdenticalCount() {
        return identicalCount;
    }
}
