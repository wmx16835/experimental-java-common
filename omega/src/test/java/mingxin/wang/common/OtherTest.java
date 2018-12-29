package mingxin.wang.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import mingxin.wang.common.json.JsonUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class OtherTest {
    private static final String INPUT_JSON = "{\n" +
            "\t\"store\": {\n" +
            "\t\t\"book\": [{\n" +
            "\t\t\t\t\"category\": \"reference\",\n" +
            "\t\t\t\t\"author\": \"Nigel Rees\",\n" +
            "\t\t\t\t\"title\": \"Sayings of the Century\",\n" +
            "\t\t\t\t\"price\": 8.95\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"category\": \"fiction\",\n" +
            "\t\t\t\t\"author\": \"Evelyn Waugh\",\n" +
            "\t\t\t\t\"title\": \"Sword of Honour\",\n" +
            "\t\t\t\t\"price\": 12.99\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"category\": \"fiction\",\n" +
            "\t\t\t\t\"author\": \"Herman Melville\",\n" +
            "\t\t\t\t\"title\": \"Moby Dick\",\n" +
            "\t\t\t\t\"isbn\": \"0-553-21311-3\",\n" +
            "\t\t\t\t\"price\": 8.99\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"category\": \"fiction\",\n" +
            "\t\t\t\t\"author\": \"J. R. R. Tolkien\",\n" +
            "\t\t\t\t\"title\": \"The Lord of the Rings\",\n" +
            "\t\t\t\t\"isbn\": \"0-395-19395-8\",\n" +
            "\t\t\t\t\"price\": 22.99\n" +
            "\t\t\t}\n" +
            "\t\t],\n" +
            "\t\t\"bicycle\": {\n" +
            "\t\t\t\"color\": \"red\",\n" +
            "\t\t\t\"price\": 19.95\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"expensive\": 10\n" +
            "}";

    private static void test(Collection<String> includes, Collection<String> excludes) throws IOException {
        ObjectMapper mapper = JsonUtils.getDefaultObjectMapper();
        JsonNode jsonNode = mapper.readTree(INPUT_JSON);
        List<JsonPath> includeJsonPaths = includes.stream().map(JsonPath::compile).collect(Collectors.toList());
        List<JsonPath> excludeJsonPaths = excludes.stream().map(JsonPath::compile).collect(Collectors.toList());
        System.out.println(mapper.writeValueAsString(JsonUtils.selectPath(jsonNode, includeJsonPaths, excludeJsonPaths)));
    }

    private static final Configuration JSON_PATH_CONFIGURATION = Configuration.builder()
            .options(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST)
            .jsonProvider(new JacksonJsonNodeJsonProvider(JsonUtils.getDefaultObjectMapper()))
            .build();
    public static void main(String[] args) throws IOException {
        test(
                ImmutableList.of("$.store.book", "$.expensive"),
                ImmutableList.of("$.store.book[?(@.price < 10)]"));
    }
}
