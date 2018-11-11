package mingxin.wang.common.html;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
// Web页面静态元素构造器
public final class StaticElementBuilder {
    private StringBuilder result;
    private String tag;

    private StaticElementBuilder(StringBuilder result, String tag) {
        this.result = result;
        this.tag = tag;
    }

    public static StaticElementBuilder of(String tag) {
        return new StaticElementBuilder(new StringBuilder("<" + tag), tag);
    }

    public StaticElementBuilder addAttribute(String name, String value) {
        result.append(" ")
                .append(name)
                .append("=\"")
                .append(value)
                .append("\"");
        return this;
    }

    public String build() {
        return result.append(" />").toString();
    }

    public String build(String content) {
        return result.append(">")
                .append(content)
                .append("</")
                .append(tag)
                .append(">")
                .toString();
    }
}
