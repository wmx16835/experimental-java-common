package mingxin.wang.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
final class ObjectJsonDifferenceIgnoreIdentical extends JsonDifference {
    private final Map<String, JsonNode> leftOnly;
    private final Map<String, JsonNode> rightOnly;
    private final Map<String, JsonDifference> different;
    private final int identicalCount;

    ObjectJsonDifferenceIgnoreIdentical(ObjectJsonDifference original) {
        super(Type.OBJECT);
        this.leftOnly = original.getLeftOnly();
        this.rightOnly = original.getRightOnly();
        this.different = Maps.newTreeMap();
        for (Map.Entry<String, StdJsonDifference> entry : original.getDifferent().entrySet()) {
            this.different.put(entry.getKey(), JsonUtils.foldIdentical(entry.getValue()));
        }
        this.identicalCount = original.getIdentical().size();
    }

    public Map<String, JsonNode> getLeftOnly() {
        return leftOnly;
    }

    public Map<String, JsonNode> getRightOnly() {
        return rightOnly;
    }

    public Map<String, JsonDifference> getDifferent() {
        return different;
    }

    public int getIdenticalCount() {
        return identicalCount;
    }
}
