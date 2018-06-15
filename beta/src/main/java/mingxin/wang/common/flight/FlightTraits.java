package mingxin.wang.common.flight;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class FlightTraits {
    private static final String CONNECTION_TOKEN = "/";
    private static final String LEG_TOKEN = "~";
    private static final Splitter CONNECTION_SPLITTER = Splitter.on(CONNECTION_TOKEN);
    private static final Splitter LEG_SPLITTER = Splitter.on(LEG_TOKEN);
    private static final Joiner CONNECTION_JOINER = Joiner.on(CONNECTION_TOKEN);
    private static final Joiner LEG_JOINER = Joiner.on(LEG_TOKEN);

    public static List<List<String>> decode(String s) {
        List<List<String>> result = new ArrayList<>();
        for (String leg : LEG_SPLITTER.split(s)) {
            result.add(CONNECTION_SPLITTER.splitToList(leg));
        }
        return result;
    }

    public static String encode(Collection<? extends Collection<?>> traits) {
        List<String> legs = new ArrayList<>();
        for (Collection<?> leg : traits) {
            legs.add(CONNECTION_JOINER.join(leg));
        }
        return LEG_JOINER.join(legs);
    }

    public static boolean isBijective(Collection<? extends Collection<?>> traitsA, Collection<? extends Collection<?>> traitsB) {
        if (traitsA.size() != traitsB.size()) {
            return false;
        }
        Iterator<? extends Collection<?>> iteratorA = traitsA.iterator();
        Iterator<? extends Collection<?>> iteratorB = traitsB.iterator();
        while (iteratorA.hasNext()) {
            if (iteratorA.next().size() != iteratorB.next().size()) {
                return false;
            }
        }
        return true;
    }

    public static <T> List<List<T>> makeBijective(Collection<T> source, Collection<? extends Collection<?>> other) {
        int totalCount = 0;
        for (Collection<?> leg : other) {
            totalCount += leg.size();
        }
        if (totalCount != source.size()) {
            return null;
        }
        Iterator<T> sourceIterator = source.iterator();
        List<List<T>> result = new ArrayList<>();
        for (Collection<?> leg : other) {
            List<T> current = new ArrayList<>();
            for (int i = 0; i < leg.size(); ++i) {
                current.add(sourceIterator.next());
            }
            result.add(current);
        }
        return result;
    }

    private FlightTraits() {
        throw new UnsupportedOperationException();
    }
}
