package mingxin.wang.common.html;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
// Web页面静态元素构造器
public final class StaticElementBuilder {
    private StringBuilder result;
    private String tag;
    private StringBuilder content;

    private StaticElementBuilder(StringBuilder result, String tag, StringBuilder content) {
        this.result = result;
        this.tag = tag;
        this.content = content;
    }

    public static StaticElementBuilder of(String tag) {
        return new StaticElementBuilder(new StringBuilder("<" + tag), tag, new StringBuilder());
    }

    public StaticElementBuilder addAttribute(String name, String value) {
        result.append(" ")
                .append(name)
                .append("=\"")
                .append(value)
                .append("\"");
        return this;
    }

    public StaticElementBuilder addContent(String content) {
        this.content.append(System.lineSeparator()).append(content);
        return this;
    }

    public StaticElementBuilder addContent(StaticElementBuilder builder) {
        return addContent(builder.build());
    }

    public String build() {
        if (content.length() == 0) {
            result.append(" />");
        } else {
            result.append(">")
                    .append(content)
                    .append(System.lineSeparator())
                    .append("</")
                    .append(tag)
                    .append(">");
        }
        return result
                .append(System.lineSeparator())
                .toString();
    }
}
