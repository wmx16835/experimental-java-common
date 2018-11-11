package mingxin.wang.common.html;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */

// Web页面常用静态元素工厂
public final class SimpleElementFactory {
    private static final DateTimeFormatter NORMAL_DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

    private SimpleElementFactory() {
    }

    public static String makeHeader(String title) {
        String head = "<meta charset=\"UTF-8\">" + StaticElementBuilder.of("title").build(title);
        return "<!DOCTYPE html>" + System.lineSeparator() + StaticElementBuilder.of("head").build(head) + System.lineSeparator();
    }

    public static String makeLabel(String name) {
        return StaticElementBuilder.of("label").build(name);
    }

    public static String makePostForm(String action, String... items) {
        StaticElementBuilder result = StaticElementBuilder.of("form")
                .addAttribute("action", action)
                .addAttribute("method", "post");
        StringBuilder content = new StringBuilder();
        for (String item : items) {
            content.append(makeParagraph(item));
        }
        return result.build(content.toString());
    }

    public static String makeFilePostForm(String action, String name, Iterable<Pair<String, String>> pairs) {
        StaticElementBuilder result = StaticElementBuilder.of("form")
                .addAttribute("action", action)
                .addAttribute("method", "post")
                .addAttribute("enctype", "multipart/form-data");
        StringBuilder content = new StringBuilder();
        for (Pair<String, String> pair : pairs) {
            content.append(makeParagraph(makeLabel(pair.getLeft()) + makeFileInput(pair.getRight())));
        }
        content.append(makeSubmitButton(name));
        return result.build(content.toString());
    }

    public static String makeLink(String display, String uri) {
        return makeLink(display, uri, false);
    }

    public static String makeLink(String display, String uri, boolean newWindow) {
        StaticElementBuilder result = StaticElementBuilder.of("a").addAttribute("href", uri);
        if (newWindow) {
            result.addAttribute("target", "_blank");
        }
        return result.build(display);
    }

    public static String makeLine() {
        return StaticElementBuilder.of("hr").build();
    }

    public static String makeParagraph(String content) {
        return StaticElementBuilder.of("p").build(content);
    }

    public static String makeText(String data) {
        return StaticElementBuilder.of("pre").build(
                data.replaceAll("&", "&amp;")
                        .replaceAll("<", "&lt;")
                        .replaceAll(">", "&gt;"));
    }

    public static String makeDateInput(String name) {
        return makeInputBuilder("date", name, null, null).build();
    }

    public static String makeDateInput(String name, LocalDate defaultValue) {
        return makeInputBuilder("date", name, null, NORMAL_DATE_FORMAT.print(defaultValue)).build();
    }

    public static String makeFileInput(String name) {
        return makeInputBuilder("file", name, null, null).build();
    }

    public static String makeHiddenInput(String name, String placeholder, String value) {
        return makeInputBuilder("hidden", name, placeholder, value).build();
    }

    public static String makeTextInput(String name, String placeholder, String value) {
        return makeInputBuilder("text", name, placeholder, value).build();
    }

    public static String makeTextArea(String name, String placeholder, String value) {
        StaticElementBuilder builder = StaticElementBuilder.of("textarea").addAttribute("name", name);
        if (placeholder != null) {
            builder.addAttribute("placeholder", placeholder);
        }
        return builder.build(value == null ? "" : value);
    }

    public static String makePasswordInput(String name) {
        return makeInputBuilder("password", name, null, null).build();
    }

    public static String makeSubmitButton(String name) {
        return makeInputBuilder("submit", null, null, name).build();
    }

    public static String makeRadioInput(String name, String selected, Iterable<? extends Pair<String, String>> options) {
        StringBuilder result = new StringBuilder();
        for (Pair<String, String> option : options) {
            StaticElementBuilder builder = makeInputBuilder("radio", name, null, option.getValue());
            if (option.getValue().equals(selected)) {
                builder.addAttribute("checked", "checked");
            }
            result.append(builder.build());
            result.append(option.getKey());
        }
        return result.toString();
    }

    public static String makeSelectInput(String name, String selected, Iterable<? extends Pair<String, String>> options) {
        StaticElementBuilder result = StaticElementBuilder.of("select").addAttribute("name", name);
        StringBuilder content = new StringBuilder();
        for (Pair<String, String> option : options) {
            StaticElementBuilder builder = StaticElementBuilder.of("option").addAttribute("value", option.getValue());
            if (option.getValue().equals(selected)) {
                builder.addAttribute("selected", "selected");
            }
            content.append(builder.build(option.getKey()));
        }
        return result.build(content.toString());
    }

    private static StaticElementBuilder makeInputBuilder(String type, String name, String placeholder, String value) {
        StaticElementBuilder result = StaticElementBuilder.of("input").addAttribute("type", type);
        if (name != null) {
            result.addAttribute("name", name);
        }
        if (placeholder != null) {
            result.addAttribute("placeholder", placeholder);
        }
        if (value != null) {
            result.addAttribute("value", value);
        }
        return result;
    }
}
