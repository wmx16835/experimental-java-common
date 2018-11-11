package mingxin.wang.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class ObjectJsonDifference extends StdJsonDifference {
    private final Map<String, JsonNode> leftOnly;
    private final Map<String, JsonNode> rightOnly;
    private final Map<String, StdJsonDifference> different;
    private final Map<String, JsonNode> identical;

    @Override
    public ObjectJsonDifference castToObjectDifference() {
        return this;
    }

    public Map<String, JsonNode> getLeftOnly() {
        return leftOnly;
    }

    public Map<String, JsonNode> getRightOnly() {
        return rightOnly;
    }

    public Map<String, StdJsonDifference> getDifferent() {
        return different;
    }

    public Map<String, JsonNode> getIdentical() {
        return identical;
    }

    static ObjectJsonDifference compare(JsonNode leftNode, JsonNode rightNode) {
        Map<String, JsonNode> leftFields = getFields(leftNode);
        Map<String, JsonNode> rightFields = getFields(rightNode);
        Map<String, JsonNode> leftOnly = Maps.newTreeMap();
        Map<String, JsonNode> rightOnly = Maps.newTreeMap();
        Map<String, StdJsonDifference> different = Maps.newTreeMap();
        Map<String, JsonNode> identical = Maps.newTreeMap();
        for (Map.Entry<String, JsonNode> entry : leftFields.entrySet()) {
            if (!rightFields.containsKey(entry.getKey())) {
                leftOnly.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, JsonNode> entry : rightFields.entrySet()) {
            if (!leftFields.containsKey(entry.getKey())) {
                rightOnly.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, JsonNode> leftEntry : leftFields.entrySet()) {
            String key = leftEntry.getKey();
            JsonNode leftValue = leftEntry.getValue();
            JsonNode rightValue = rightFields.get(key);
            if (rightValue != null) {
                if (leftValue.equals(rightValue)) {
                    identical.put(key, leftValue);
                } else {
                    different.put(key, JsonUtils.compare(leftValue, rightValue));
                }
            }
        }
        return new ObjectJsonDifference(leftOnly, rightOnly, different, identical);
    }

    private static Map<String, JsonNode> getFields(JsonNode jsonNode) {
        HashMap<String, JsonNode> result = Maps.newHashMap();
        Iterator<Map.Entry<String, JsonNode>> iterator = jsonNode.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private ObjectJsonDifference(Map<String, JsonNode> leftOnly, Map<String, JsonNode> rightOnly,
                                 Map<String, StdJsonDifference> different, Map<String, JsonNode> identical) {
        super(Type.OBJECT);
        this.leftOnly = leftOnly;
        this.rightOnly = rightOnly;
        this.different = different;
        this.identical = identical;
    }
}
