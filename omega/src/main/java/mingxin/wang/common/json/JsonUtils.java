package mingxin.wang.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class JsonUtils {
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    private static final Configuration JSON_PATH_CONFIGURATION = Configuration.builder()
            .options(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST)
            .jsonProvider(new JacksonJsonNodeJsonProvider(JsonUtils.getDefaultObjectMapper()))
            .build();

    public static ObjectMapper getDefaultObjectMapper() {
        return DEFAULT_OBJECT_MAPPER;
    }

    public static JsonNode selectPath(JsonNode jsonNode, Iterable<JsonPath> includes, Iterable<JsonPath> excludes) {
        JsonNodeSelectionContext context = JsonNodeSelectionContext.of(jsonNode);
        for (JsonPath jsonPath : includes) {
            mark(context, jsonNode, jsonPath, true);
        }
        for (JsonPath jsonPath : excludes) {
            mark(context, jsonNode, jsonPath, false);  // May override includes
        }
        JsonNode result = context.resolve(false);
        return result == null ? NullNode.getInstance() : result;
    }

    private static void mark(JsonNodeSelectionContext context, JsonNode jsonNode, JsonPath jsonPath, boolean included) {
        for (JsonNode path : jsonPath.<ArrayNode>read(jsonNode, JSON_PATH_CONFIGURATION)) {
            JsonNodeSelectionContext currentContext = context;
            for (JsonPathSegment segment : JsonPathSegment.parse(path.asText())) {
                if (segment.type == JsonPathSegment.Type.ATTRIBUTE) {
                    currentContext = currentContext.fields.get(segment.attribute);
                } else {
                    currentContext = currentContext.elements[segment.index];
                }
            }
            currentContext.state = included ? JsonNodeSelectionContext.State.INCLUDED : JsonNodeSelectionContext.State.EXCLUDED;
        }
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
