package mingxin.wang.common;

import mingxin.wang.common.serialization.Jsons;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class JsonsTest {
    private void validate(Object o) {
        System.out.println("------");
        try {
            String json = Jsons.getDefaultObjectMapper().writeValueAsString(o);
            System.out.println("Original JSON: " + json);
            System.out.println("Parsed Data:");
            int total = 0;
            for (Map<String, Object> map : Jsons.readAlongAllPaths(json)) {
                System.out.println("Item " + (++total) + " with " + map.size() + " properties:");
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    System.out.println("\t" + entry.getKey() + " : " + entry.getValue() + " (" + entry.getValue().getClass() + ")");
                }
            }
            System.out.println("Total count: " + total);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Data
    @AllArgsConstructor
    private static class Bean1 {
        private int bean1Prop1;
        private double bean1Prop2;
        private BigInteger bean1Prop3;
    }

    @Data
    @AllArgsConstructor
    private static class Bean2 {
        private int bean2Prop1;
        private Bean1[] bean2Prop2;
        private boolean bean2Prop3;
        private Date bean2Prop4;
    }

    @Data
    @AllArgsConstructor
    private static class Bean3 {
        private List<Bean1> bean3Prop1;
        private Bean1[] bean3Prop2;
        private Bean2 bean3Prop3;
        private int bean3Prop4;
    }

    @Test
    public void testOne() {
        Bean1 bean1 = new Bean1(123, 3.14, new BigInteger("14159265358979323846"));
        validate(bean1);
    }

    @Test
    public void testMany() {
        Bean1 bean11 = new Bean1(1001, 1e9, BigInteger.ONE);
        Bean1 bean12 = new Bean1(2002, 1e9 + 7, new BigInteger("26433832795028841971"));
        Bean1 bean13 = new Bean1(123456, 0.1, new BigInteger("-1"));
        Bean2 bean2 = new Bean2(123, new Bean1[]{bean11, bean12, bean13}, true, new Date());
        validate(bean2);
    }

    @Test
    public void testComplex() {
        Bean1 bean11 = new Bean1(1001, 1e9, BigInteger.ONE);
        Bean1 bean12 = new Bean1(2002, 1e9 + 7, new BigInteger("26433832795028841971"));
        Bean1 bean13 = new Bean1(123456, 0.1, new BigInteger("-1"));
        Bean1 bean14 = new Bean1(123, 3.14, new BigInteger("14159265358979323846"));
        ArrayList<Bean1> bean1List = new ArrayList<>();
        bean1List.add(bean11);
        Bean1[] bean1Array1 = { bean12, bean13 };
        Bean1[] bean1Array2 = { bean14 };
        Bean2 bean2 = new Bean2(123, bean1Array1, true, new Date());
        Bean3 bean3 = new Bean3(bean1List, bean1Array2, bean2, 666);
        validate(bean3);
    }
}
