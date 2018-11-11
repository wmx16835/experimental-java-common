package mingxin.wang.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class ArrayJsonDifference extends StdJsonDifference {
    private final List<JsonNode> leftOnly;
    private final List<JsonNode> rightOnly;
    private final List<JsonNode> identical;

    @Override
    public ArrayJsonDifference castToArrayDifference() {
        return this;
    }

    public List<JsonNode> getLeftOnly() {
        return leftOnly;
    }

    public List<JsonNode> getRightOnly() {
        return rightOnly;
    }

    public List<JsonNode> getIdentical() {
        return identical;
    }

    static ArrayJsonDifference compare(JsonNode leftNode, JsonNode rightNode) {
        Set<JsonNode> leftElements = getElements(leftNode);
        Set<JsonNode> rightElements = getElements(rightNode);
        List<JsonNode> leftOnly = Lists.newArrayList();
        List<JsonNode> rightOnly = Lists.newArrayList();
        List<JsonNode> identical = Lists.newArrayList();
        for (JsonNode element : leftElements) {
            if (!rightElements.contains(element)) {
                leftOnly.add(element);
            } else {
                identical.add(element);
            }
        }
        for (JsonNode element : rightElements) {
            if (!leftElements.contains(element)) {
                rightOnly.add(element);
            }
        }
        return new ArrayJsonDifference(leftOnly, rightOnly, identical);
    }

    private static Set<JsonNode> getElements(JsonNode jsonNode) {
        Set<JsonNode> result = Sets.newHashSet();
        for (JsonNode element : jsonNode) {
            result.add(element);
        }
        return result;
    }

    private ArrayJsonDifference(List<JsonNode> leftOnly, List<JsonNode> rightOnly, List<JsonNode> identical) {
        super(Type.ARRAY);
        this.leftOnly = leftOnly;
        this.rightOnly = rightOnly;
        this.identical = identical;
    }
}
