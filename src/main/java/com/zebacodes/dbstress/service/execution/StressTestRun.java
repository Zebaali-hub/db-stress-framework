package com.zebacodes.dbstress.service.execution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class StressTestRun {

    private final CountDownLatch stopSignal;
    private final AtomicInteger activeThreads;
    private final AtomicReference<String> failureMessage;
    private final CompletableFuture<ExecutionSummary> completion;

    public StressTestRun(CountDownLatch stopSignal, AtomicInteger activeThreads,
                         AtomicReference<String> failureMessage,
                         CompletableFuture<ExecutionSummary> completion) {
        this.stopSignal = stopSignal;
        this.activeThreads = activeThreads;
        this.failureMessage = failureMessage;
        this.completion = completion;
    }

    public void stop() {
        stopSignal.countDown();
    }

    public boolean isStopRequested() {
        return stopSignal.getCount() == 0;
    }

    public int activeThreads() {
        return activeThreads.get();
    }

    public String failureMessage() {
        return failureMessage.get();
    }

    public CompletableFuture<ExecutionSummary> completion() {
        return completion;
    }
}
