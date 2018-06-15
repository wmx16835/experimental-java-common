package mingxin.wang.common.html;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class TableBuilder {
    private int border;
    private ArrayList<String> labels;
    private ArrayList<ArrayList<String>> content = new ArrayList<>();

    public TableBuilder(int border, Collection<? extends String> labels) {
        this.border = border;
        this.labels = new ArrayList<>(labels);
    }

    private static StaticElementBuilder makeRow(String tag, Iterable<String> items) {
        StaticElementBuilder result = StaticElementBuilder.of("tr");
        for (String item : items) {
            result.addContent(StaticElementBuilder.of(tag).addContent(item).build());
        }
        return result;
    }

    public void addRow(Collection<? extends String> row) {
        Preconditions.checkState(row.size() == labels.size());
        content.add(new ArrayList<>(row));
    }

    public void addColumn(String label, Collection<? extends String> column) {
        Preconditions.checkState(column.size() == content.size());
        labels.add(label);
        Iterator<? extends String> columnIterator = column.iterator();
        for (ArrayList<String> row : content) {
            row.add(columnIterator.next());
        }
    }

    public String build() {
        StaticElementBuilder result = StaticElementBuilder.of("table").addAttribute("border", "" + border);
        result.addContent(makeRow("th", labels));
        for (Iterable<String> row : content) {
            result.addContent(makeRow("td", row));
        }
        return result.build();
    }
}
