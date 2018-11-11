package mingxin.wang.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;

import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class JsonUtils {
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    public static ObjectMapper getDefaultObjectMapper() {
        return DEFAULT_OBJECT_MAPPER;
    }

    public static JsonNode selectPath(JsonNode jsonNode, Iterable<JsonPath> includes, Iterable<JsonPath> excludes) {
        JsonNodeFilterContext node = JsonNodeFilterContext.of(jsonNode);
        for (JsonPath jsonPath : includes) {
            node.mark(jsonPath, true);
        }
        for (JsonPath jsonPath : excludes) {
            node.mark(jsonPath, false); // May override includes
        }
        return node.resolve(false) ? jsonNode : NullNode.getInstance();
    }

    public static JsonNode convertArrayIntoObject(JsonNode jsonNode) {
        if (!jsonNode.isContainerNode()) {
            return jsonNode;
        }
        ObjectNode result = ((ContainerNode) jsonNode).objectNode();
        if (jsonNode.isArray()) {
            int index = 0;
            for (JsonNode child : jsonNode) {
                result.set(Integer.toString(index), convertArrayIntoObject(child));
                ++index;
            }
        } else {
            Iterator<Map.Entry<String, JsonNode>> iterator = jsonNode.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                result.set(entry.getKey(), convertArrayIntoObject(entry.getValue()));
            }
        }
        return result;
    }

    public static StdJsonDifference compare(JsonNode leftNode, JsonNode rightNode) {
        if (leftNode.equals(rightNode)) {
            return new NoneJsonDifference(leftNode);
        }
        if (leftNode.isObject() && rightNode.isObject()) {
            return ObjectJsonDifference.compare(leftNode, rightNode);
        }
        if (leftNode.isArray() && rightNode.isArray()) {
            return ArrayJsonDifference.compare(leftNode, rightNode);
        }
        return new EntiretyJsonDifference(leftNode, rightNode);
    }

    public static JsonDifference foldIdentical(StdJsonDifference original) {
        switch (original.getDifferenceType()) {
            case OBJECT: return new ObjectJsonDifferenceIgnoreIdentical(original.castToObjectDifference());
            case ARRAY: return new ArrayJsonDifferenceIgnoreIdentical(original.castToArrayDifference());
            case NONE: return NoneJsonDifferenceIgnoreIdentical.INSTANCE;
            default: return original;
        }
    }

    private JsonUtils() {
        throw new UnsupportedOperationException();
    }
}
