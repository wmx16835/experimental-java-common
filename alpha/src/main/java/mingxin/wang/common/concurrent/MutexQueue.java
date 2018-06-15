package mingxin.wang.common.concurrent;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class MutexQueue<T> {
    private static final AtomicReferenceFieldUpdater<Node, Node> FORWARD_LIST_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");

    private Node<T> head = new Node<>(null);
    private volatile Node<T> tail = head;

    public void offer(T data) {
        Node<T> current = new Node<>(data);
        while (!FORWARD_LIST_UPDATER.weakCompareAndSet(tail, null, current)) {}
        tail = current;
    }

    public T pull() {
        T result = (head = head.next).data;
        head.data = null;  // For GC
        return result;
    }

    private static final class Node<T> {
        private T data;
        volatile Node<T> next;

        private Node(T data) {
            this.data = data;
        }
    }
}
