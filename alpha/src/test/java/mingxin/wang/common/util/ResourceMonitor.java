package mingxin.wang.common.util;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class ResourceMonitor {
    private String name;
    private long time;
    private double startMemory;
    private volatile double endMemory;
    private volatile boolean running;

    public ResourceMonitor(String name) {
        reset(name);
        this.running = true;
        startWorker();
    }

    public void record() {
        if (!running) {
            throw new RuntimeException();
        }
        System.out.println("Event \"" + name + "\" finished in " + (System.currentTimeMillis() - time) + " milliseconds, memory: " + (endMemory - startMemory) + " MB");
    }

    public void stop() {
        running = false;
    }

    public void reset(String name) {
        this.name = name;
        this.time = System.currentTimeMillis();
        this.startMemory = Runtime.getRuntime().freeMemory() / 1024. / 1024.;
        this.endMemory = this.startMemory;
    }

    private void startWorker() {
        Thread thread = new Thread(() -> {
            while (running) {
                double currentMemory = Runtime.getRuntime().freeMemory() / 1024. / 1024.;
                if (currentMemory > endMemory) {
                    endMemory = currentMemory;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
