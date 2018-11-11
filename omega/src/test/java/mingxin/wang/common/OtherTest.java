package mingxin.wang.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class OtherTest {
    private static final class Data {
    }

    public static void main(String[] args) throws JsonProcessingException {
        try {
            throw new Exception("lalala");
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getSuppressed()));
        }

        ObjectMapper mapper = null;

        List<Data> data = null;
        byte[] toSend = mapper.writeValueAsBytes(data);
    }
}
