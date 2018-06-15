package mingxin.wang.common.parsing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class Dates {
    private static final int[] DATE_DIGIT_COUNT = {4, 2, 2};
    private static final int[] TIME_DIGIT_COUNT = {2, 2, 2, 3};
    private static final int[] TIME_ZONE_DIGIT_COUNT = {2, 2};
    private static final Range<Integer> YEAR_RANGE = Range.closed(1000, 9999);
    private static final Range<Integer> MONTH_RANGE = Range.closed(1, 12);
    private static final Range<Integer> DAY_RANGE = Range.closed(1, 31);
    private static final String[] MONTH_ABBR = {"JAN", "FEB", "MAR", "APR", "MAY", "JUNE", "JULY", "AUG", "SEPT", "OCT", "NOV", "DEC"};
    private static final ImmutableMap<String, Integer> MONTH_ABBR_TO_CODE;
    private static final int HALF_DAY_HOURS = 12;
    private static final int CENTURY_YEARS = 100;
    private static final int NEARBY_CENTURY = (new DateTime().getYear() + CENTURY_YEARS / 2) / CENTURY_YEARS * CENTURY_YEARS;
    private static final Pattern TIME_ZONE_PATTERN_WITH_GMT = Pattern.compile("GMT.*$");
    private static final Pattern TIME_ZONE_PATTERN_WITH_UTC_1 = Pattern.compile("(UTC)?[+-]\\d{1,2}:\\d{1,2}$");
    private static final Pattern TIME_ZONE_PATTERN_WITH_UTC_2 = Pattern.compile("[+-]\\d{4}$");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{1,2}:\\d{1,2}(:\\d{1,2}(.\\d{1,3})?)?");
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern MONTH_ABBR_PATTERN;
    private static final Pattern PM_PATTERN = Pattern.compile("PM");
    private static final int[] NULL_TIME = new int[TIME_DIGIT_COUNT.length];
    private static final int[] NULL_DATE = new int[DATE_DIGIT_COUNT.length];

    static {
        StringBuilder abbrPattern = new StringBuilder();
        ImmutableMap.Builder<String, Integer> monthAbbrMapBuilder = ImmutableMap.builder();
        for (int i = 0; i < MONTH_ABBR.length; ++i) {
            if (abbrPattern.length() != 0) {
                abbrPattern.append("|");
            }
            abbrPattern.append(MONTH_ABBR[i]);
            monthAbbrMapBuilder.put(MONTH_ABBR[i], i + 1);
        }
        MONTH_ABBR_PATTERN = Pattern.compile(abbrPattern.toString());
        MONTH_ABBR_TO_CODE = monthAbbrMapBuilder.build();
    }

    private static int[] parseDigits(String s, int[] digitCount) {
        int[] result = new int[digitCount.length];
        int pos = 0, count = 0;
        boolean advance;
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                result[pos] = result[pos] * 10 + c - '0';
                advance = ++count == digitCount[pos];
            } else {
                advance = count != 0;
            }
            if (advance) {
                if (++pos == result.length) {
                    break;
                }
                count = 0;
            }
        }
        return result;
    }

    private static String matchAndRemove(StringBuilder builder, Pattern pattern) {
        Matcher matcher = pattern.matcher(builder);
        if (matcher.find()) {
            String result = matcher.group();
            builder.delete(matcher.start(), matcher.end());
            return result;
        }
        return null;
    }

    private static DateTimeZone getTimeZone(StringBuilder builder) {
        String zone = matchAndRemove(builder, TIME_ZONE_PATTERN_WITH_GMT);
        if (zone != null) {
            return DateTimeZone.forTimeZone(TimeZone.getTimeZone(zone));
        }
        zone = matchAndRemove(builder, TIME_ZONE_PATTERN_WITH_UTC_1);
        if (zone == null) {
            zone = matchAndRemove(builder, TIME_ZONE_PATTERN_WITH_UTC_2);
            if (zone == null) {
                return DateTimeZone.getDefault();
            }
        }
        int[] result = parseDigits(zone, TIME_ZONE_DIGIT_COUNT);
        return DateTimeZone.forOffsetHoursMinutes(result[0], result[1]);
    }

    private static int[] getTime(StringBuilder builder) {
        String time = matchAndRemove(builder, TIME_PATTERN);
        if (time == null) {
            return NULL_TIME;
        }
        int[] result = parseDigits(time, TIME_DIGIT_COUNT);
        if (matchAndRemove(builder, PM_PATTERN) != null) {
            result[0] += HALF_DAY_HOURS;
        }
        return result;
    }

    private static int[] getDate(StringBuilder builder) {
        int[] result = parseDigits(builder.toString(), DATE_DIGIT_COUNT);
        if (result[0] < CENTURY_YEARS) {
            result[0] += result[0] < CENTURY_YEARS / 2 ? NEARBY_CENTURY : NEARBY_CENTURY - CENTURY_YEARS;
        }
        if (YEAR_RANGE.contains(result[0]) && MONTH_RANGE.contains(result[1]) && DAY_RANGE.contains(result[2])) {
            return result;
        }
        String year = matchAndRemove(builder, YEAR_PATTERN);
        if (year == null) {
            return NULL_DATE;
        }
        result[0] = Integer.parseInt(year);
        String month = matchAndRemove(builder, MONTH_ABBR_PATTERN);
        if (month != null) {
            result[1] = MONTH_ABBR_TO_CODE.get(month);
        } else {
            month = matchAndRemove(builder, NUMBER_PATTERN);
            if (month == null) {
                return NULL_DATE;
            }
            result[1] = Integer.parseInt(month);
        }
        String day = matchAndRemove(builder, NUMBER_PATTERN);
        if (day == null) {
            return NULL_DATE;
        }
        result[2] = Integer.parseInt(day);
        return result;
    }

    private static boolean requireSeparator(char a, char b) {
        return (Character.isLetter(a) && Character.isLetter(b)) || (Character.isDigit(a) && Character.isDigit(b));
    }

    public static DateTime parseDateTime(String s) {
        StringBuilder builder = new StringBuilder();
        for (char c : s.trim().toCharArray()) {
            if (c <= ' ') {
                if (builder.charAt(builder.length() - 1) != ' ') {
                    builder.append(' ');
                }
            } else {
                if (Character.isLowerCase(c)) {
                    c = Character.toUpperCase(c);
                }
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ' ' && !requireSeparator(c, builder.charAt(builder.length() - 2))) {
                    builder.setCharAt(builder.length() - 1, c);
                } else {
                    builder.append(c);
                }
            }
        }
        DateTimeZone timeZone = getTimeZone(builder);
        int[] time = getTime(builder);
        int[] date = getDate(builder);
        if (date == NULL_DATE) {
            return new DateTime();
        }
        return new DateTime(date[0], date[1], date[2], time[0], time[1], time[2], time[3], timeZone);
    }

    public static int toShortDate(DateTime dateTime) {
        return dateTime.getYear() * 10000 + dateTime.getMonthOfYear() * 100 + dateTime.getDayOfMonth();
    }

    private Dates() {
        throw new UnsupportedOperationException();
    }
}
