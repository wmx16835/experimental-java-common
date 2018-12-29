package mingxin.wang.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
final class JsonNodeSelectionContext {
    enum State {
        UNSPECIFIED, INCLUDED, EXCLUDED
    }

    State state = State.UNSPECIFIED;
    private final JsonNode data;
    final Map<String, JsonNodeSelectionContext> fields;
    final JsonNodeSelectionContext[] elements;

    static JsonNodeSelectionContext of(JsonNode node) {
        switch (node.getNodeType()) {
            case OBJECT:
            case POJO:
                LinkedHashMap<String, JsonNodeSelectionContext> fields = Maps.newLinkedHashMap();
                Iterator<Map.Entry<String, JsonNode>> it = node.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    fields.put(entry.getKey(), of(entry.getValue()));
                }
                return new JsonNodeSelectionContext(null, fields, null);
            case ARRAY:
                JsonNodeSelectionContext[] elements = new JsonNodeSelectionContext[node.size()];
                int i = 0;
                for (JsonNode e : node) {
                    elements[i++] = of(e);
                }
                return new JsonNodeSelectionContext(null, null, elements);
        }
        return new JsonNodeSelectionContext(node, null, null);
    }

    JsonNode resolve(boolean parentIncluded) {
        if (state == State.INCLUDED) {
            parentIncluded = true;
        } else if (state == State.EXCLUDED) {
            return null;
        }
        if (data != null) {
            return parentIncluded ? data.deepCopy() : null;
        }
        boolean childIncluded = false;
        if (elements != null) {
            ArrayList<JsonNode> arrayData = Lists.newArrayList();
            for (JsonNodeSelectionContext element : elements) {
                JsonNode node = element.resolve(parentIncluded);
                if (node != null) {
                    childIncluded = true;
                    arrayData.add(node);
                }
            }
            return parentIncluded || childIncluded ? new ArrayNode(JsonUtils.getDefaultObjectMapper().getNodeFactory(), arrayData) : null;
        }
        LinkedHashMap<String, JsonNode> objectData = Maps.newLinkedHashMap();
        for (Map.Entry<String, JsonNodeSelectionContext> entry : fields.entrySet()) {
            JsonNode node = entry.getValue().resolve(parentIncluded);
            if (node != null) {
                childIncluded = true;
                objectData.put(entry.getKey(), node);
            }
        }
        return parentIncluded || childIncluded ? new ObjectNode(JsonUtils.getDefaultObjectMapper().getNodeFactory(), objectData) : null;
    }

    private JsonNodeSelectionContext(JsonNode data, Map<String, JsonNodeSelectionContext> fields, JsonNodeSelectionContext[] elements) {
        this.data = data;
        this.fields = fields;
        this.elements = elements;
    }
}
