package mingxin.wang.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
final class JsonNodeFilterContext {
    private static final Configuration JSON_PATH_CONFIGURATION = Configuration.builder()
            .options(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST)
            .jsonProvider(new JacksonJsonNodeJsonProvider(JsonUtils.getDefaultObjectMapper()))
            .build();

    private Boolean include = null;
    private final JsonNode data;
    private final Map<String, JsonNodeFilterContext> fields;
    private final JsonNodeFilterContext[] elements;

    static JsonNodeFilterContext of(JsonNode node) {
        switch (node.getNodeType()) {
            case OBJECT:
            case POJO:
                Map<String, JsonNodeFilterContext> fields = Maps.newHashMapWithExpectedSize(node.size());
                Iterator<Map.Entry<String, JsonNode>> it = node.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    fields.put(entry.getKey(), of(entry.getValue()));
                }
                return new JsonNodeFilterContext(node, fields, null);
            case ARRAY:
                JsonNodeFilterContext[] elements = new JsonNodeFilterContext[node.size()];
                int i = 0;
                for (JsonNode e : node) {
                    elements[i++] = of(e);
                }
                return new JsonNodeFilterContext(node, null, elements);
        }
        return new JsonNodeFilterContext(node, null, null);
    }

    void mark(JsonPath jsonPath, boolean include) {
        for (JsonNode path : jsonPath.<ArrayNode>read(data, JSON_PATH_CONFIGURATION)) {
            JsonNodeFilterContext current = this;
            for (JsonPathSegment segment : JsonPathSegment.parse(path.asText())) {
                if (segment.type == JsonPathSegment.Type.ATTRIBUTE) {
                    current = current.fields.get(segment.attribute);
                } else {
                    current = current.elements[segment.index];
                }
            }
            current.include = include;
        }
    }

    boolean resolve(boolean parentInclude) {
        if (include != null) {
            if (include) {
                parentInclude = true;
            } else {
                return false;
            }
        }
        if (data.isValueNode()) {
            return parentInclude;
        }
        boolean childInclude = false;
        if (data.isArray()) {
            ArrayNode arrayNode = (ArrayNode) data;
            for (int i = 0; i < elements.length; ++i) {
                if (!elements[i].resolve(parentInclude)) {
                    arrayNode.set(i, NullNode.getInstance());
                } else {
                    childInclude = true;
                }
            }
        } else {
            Iterator<Map.Entry<String, JsonNode>> children = data.fields();
            while (children.hasNext()) {
                Map.Entry<String, JsonNode> entry = children.next();
                if (!fields.get(entry.getKey()).resolve(parentInclude)) {
                    children.remove();
                } else {
                    childInclude = true;
                }
            }
        }
        return parentInclude || childInclude;
    }

    private JsonNodeFilterContext(JsonNode data, Map<String, JsonNodeFilterContext> fields, JsonNodeFilterContext[] elements) {
        this.data = data;
        this.fields = fields;
        this.elements = elements;
    }
}
