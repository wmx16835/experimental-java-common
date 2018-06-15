package mingxin.wang.common.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class Jsons {
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    public static ObjectMapper getDefaultObjectMapper() {
        return DEFAULT_OBJECT_MAPPER;
    }

    private Jsons() {}

    private static final class FlatReaderState {
        private String name;
        private JsonNode jsonNode;
        private Map<String, Object> properties;

        private FlatReaderState(String name, JsonNode jsonNode, Map<String, Object> properties) {
            this.name = name;
            this.jsonNode = jsonNode;
            this.properties = properties;
        }

        private void dfs(List<FlatReaderState> nextStates, String name, JsonNode jsonNode) {
            switch (jsonNode.getNodeType()) {
                case ARRAY:
                    for (JsonNode arrayElements : jsonNode) {
                        nextStates.add(new FlatReaderState(name, arrayElements, null));
                    }
                    break;
                case BINARY: case STRING:
                    properties.put(name, jsonNode.textValue());
                    break;
                case BOOLEAN:
                    properties.put(name, jsonNode.booleanValue());
                    break;
                case NUMBER:
                    properties.put(name, jsonNode.numberValue());
                    break;
                case POJO: case OBJECT:
                    Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> next = fields.next();
                        dfs(nextStates, next.getKey(), next.getValue());
                    }
                    break;
                case NULL: case MISSING:
                    properties.put(name, null);
            }
        }

        private List<FlatReaderState> advance() {
            List<FlatReaderState> result = new ArrayList<>();
            dfs(result, name, jsonNode);
            for (FlatReaderState nextState : result) {
                nextState.properties = new HashMap<>(properties);
            }
            return result;
        }
    }

    public static Iterable<Map<String, Object>> readAlongAllPaths(String json) throws IOException {
        JsonNode initialNode = getDefaultObjectMapper().readTree(json);
        return () -> new Iterator<Map<String, Object>>() {
            private Queue<FlatReaderState> remainingStates;

            {
                remainingStates = new ArrayDeque<>();
                remainingStates.add(new FlatReaderState(null, initialNode, new HashMap<>()));
            }

            @Override
            public boolean hasNext() {
                return !remainingStates.isEmpty();
            }

            @Override
            public Map<String, Object> next() {
                if (remainingStates.isEmpty()) {
                    throw new NoSuchElementException();
                }
                for (;;) {
                    FlatReaderState currentState = remainingStates.poll();
                    List<FlatReaderState> nextStates = currentState.advance();
                    if (nextStates.isEmpty()) {
                        return currentState.properties;
                    }
                    remainingStates.addAll(nextStates);
                }
            }
        };
    }
}
