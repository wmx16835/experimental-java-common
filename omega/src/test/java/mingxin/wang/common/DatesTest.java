package mingxin.wang.common;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class DatesTest {
    public static void main(String[] args) {
        DateTimeFormatter yyyyMMdd = DateTimeFormat.forPattern("yyyyMMdd");
        LocalDate date1 = LocalDate.parse("20180102", yyyyMMdd);
        LocalDate date2 = LocalDate.parse("20170102", yyyyMMdd);
        System.out.println(Days.daysBetween(date1, date2).getDays());
    }
}
