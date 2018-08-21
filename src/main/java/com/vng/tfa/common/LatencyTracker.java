package com.vng.tfa.common;

import java.util.concurrent.atomic.AtomicLong;

public class LatencyTracker {

    private long latencyPeakTime = 0L;
    private long latencyPeak = 0L;
    private long rpsPeakTime = 0L;
    private long rpsPeak = 0L;
    private final AtomicLong curSec = new AtomicLong(-1L);
    private final AtomicLong opCount = new AtomicLong(0L);
    private final AtomicLong totalLatency = new AtomicLong(0L);
    private long lastLatency = 0L;
    private long lastOpCount = 0L;
    private long lastSec = 0L;
    private long opsPerSec = 0L;

    public LatencyTracker() {
        this.lastSec = System.currentTimeMillis();
    }

    public void clear() {
        this.latencyPeakTime = 0L;
        this.latencyPeak = 0L;
        this.rpsPeak = 0L;
        this.rpsPeakTime = 0L;
        this.lastLatency = 0L;
        this.lastOpCount = 0L;
        this.lastSec = 0L;
        this.opsPerSec = 0L;
        this.curSec.set(-1L);
        this.opCount.set(0L);
        this.totalLatency.set(0L);
    }

    public void addNano(long nanos) {
        addMicro(nanos / 1000L);
    }

    public void addMicro(long micros) {
        this.opCount.incrementAndGet();
        this.totalLatency.addAndGet(micros);
        if (micros > this.latencyPeak) {
            this.latencyPeak = micros;
            this.latencyPeakTime = System.currentTimeMillis();
        }
        autoCalculate();
    }

    public long getOpCount() {
        return this.opCount.get();
    }

    public long getTotalLatencyMicros() {
        return this.totalLatency.get();
    }

    public double getAvgLatency() {
        long ops = this.opCount.get();
        long n = this.totalLatency.get();
        if (ops == 0L) {
            return 0.0D;
        }
        return n / ops;
    }

    public double getRecentLatencyMicros() {
        long ops = this.opCount.get();
        long n = this.totalLatency.get();
        long sec = System.currentTimeMillis() / 1000L;

        double ret = 0.0D;
        try {
            if ((sec - this.lastSec) != 0L) {
                this.opsPerSec = ((ops - this.lastOpCount) / (sec - this.lastSec));
            }
            if (this.opsPerSec > this.rpsPeak) {
                this.rpsPeak = this.opsPerSec;
                this.rpsPeakTime = System.currentTimeMillis();
            }
            if (ops - this.lastOpCount != 0L) {
                return (n - this.lastLatency) / (ops - this.lastOpCount);
            }
        } finally {
            this.lastLatency = n;
            this.lastOpCount = ops;
            this.lastSec = sec;
        }
        return ret;
    }

    public long getLatencyPeak() {
        return this.latencyPeak;
    }

    public long getLatencyPeakTime() {
        return this.latencyPeakTime;
    }

    public long getRPSPeak() {
        return this.rpsPeak;
    }

    public long getRPSPeakTime() {
        return this.rpsPeakTime;
    }

    private void autoCalculate() {
        long sec = System.currentTimeMillis() / 1000L;
        long cur = this.curSec.get();
        if (cur == -1L) {
            this.curSec.set(sec);
        } else if (cur != sec) {
            getRecentLatencyMicros();
            this.curSec.getAndSet(sec);
        }
    }

    public long getTotalOpsPerSec() {
        return this.opsPerSec;
    }
}
