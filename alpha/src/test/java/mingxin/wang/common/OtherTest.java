package mingxin.wang.common;

import com.google.common.collect.ImmutableMap;
import mingxin.wang.common.concurrent.ConcurrentInvoker;
import mingxin.wang.common.util.ResourceMonitor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class OtherTest {
    private static final int TEST_TIME = 100000000;
    private static final int CONCURRENCY = 50;
    private static final String TEST_KEY = "TEST_KEY";
    private static final Map<String, String> TEST_DATA = ImmutableMap.of(TEST_KEY, "foobar");

    private ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
    private ThreadLocal<Map<String, String>> threadLocal = ThreadLocal.withInitial(() -> TEST_DATA);
    private volatile Map<String, String> volatileVar = TEST_DATA;

    @Test
    public void testThreadLocal() throws InterruptedException {
        ConcurrentInvoker invoker1 = new ConcurrentInvoker();
        ConcurrentInvoker invoker2 = new ConcurrentInvoker();
        for (int i = 0; i < CONCURRENCY; ++i) {
            invoker1.add(executor, () -> {
                for (int j = 0; j < TEST_TIME; ++j) {
                    threadLocal.get().get(TEST_KEY);
                }
            });
            invoker2.add(executor, () -> {
                for (int j = 0; j < TEST_TIME; ++j) {
                    volatileVar.get(TEST_KEY);
                }
            });
        }
        ResourceMonitor recorder = new ResourceMonitor("thread local");
        invoker1.syncInvoke();
        recorder.record();
        recorder.reset("volatile");
        invoker2.syncInvoke();
        recorder.record();
    }

    private static class T {
        int a;
        double b;
        BigInteger c;

        private T(int a, double b, BigInteger c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(OtherTest.class);
        logger.info("{}", new T(1, 2, BigInteger.TEN));
        System.out.println(System.nanoTime());
    }
}
