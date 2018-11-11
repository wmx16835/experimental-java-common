package mingxin.wang.common.json;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
final class JsonPathSegment {
    enum Type {
        ATTRIBUTE, INDEX
    }

    final Type type;
    final String attribute;
    final int index;

    static List<JsonPathSegment> parse(String stdJsonPath) {  // Known bug (JsonPath): parsing fails if an attribute contains "']"
        ArrayList<JsonPathSegment> result = Lists.newArrayList();
        int i = 1;  // The first character shall always be '$'
        while (i < stdJsonPath.length()) {
            if (stdJsonPath.charAt(i + 1) == '\'') {  // Attribute
                int j = stdJsonPath.indexOf("']", i + 2);
                result.add(new JsonPathSegment(JsonPathSegment.Type.ATTRIBUTE, stdJsonPath.substring(i + 2, j), -1));
                i = j + 2;
            } else {  // Index
                int j = stdJsonPath.indexOf("]", i + 1);
                result.add(new JsonPathSegment(JsonPathSegment.Type.INDEX, null, Integer.parseInt(stdJsonPath.substring(i + 1, j))));
                i = j + 1;
            }
        }
        return result;
    }

    private JsonPathSegment(Type type, String attribute, int index) {
        this.type = type;
        this.attribute = attribute;
        this.index = index;
    }
}
