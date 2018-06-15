package mingxin.wang.common;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Arrays;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class OtherTest {
    public static void main(String[] args) throws JsonProcessingException {
        try {
            throw new Exception("lalala");
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getSuppressed()));
        }
    }
}
