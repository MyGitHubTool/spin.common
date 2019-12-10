package org.spin.core.base;

import org.spin.core.Assert;

import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

public final class Stopwatch {
    private final Ticker ticker;
    private boolean isRunning;
    private long elapsedNanos;
    private long startTick;
    private long[] records = new long[100];
    private int recordLen = 0;

    public static Stopwatch createUnstarted() {
        return new Stopwatch();
    }

    public static Stopwatch createUnstarted(Ticker ticker) {
        return new Stopwatch(ticker);
    }

    public static Stopwatch createStarted() {
        return new Stopwatch().start();
    }

    public static Stopwatch createStarted(Ticker ticker) {
        return new Stopwatch(ticker).start();
    }

    private Stopwatch() {
        this.ticker = Ticker.SYSTEM_TICKER;
    }

    private Stopwatch(Ticker ticker) {
        this.ticker = Assert.notNull(ticker, "ticker");
    }

    /**
     * 如果秒表正在计时(调用过{@link #start()}，并且自上次调用{@code start()}后，没有调用过{@link #stop()})，返回{@code true}
     *
     * @return 是否正在计时
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 启动当前{@code Stopwatch}实例
     *
     * @return 当前{@code Stopwatch}实例
     * @throws IllegalStateException 如果当前{@code Stopwatch}实例已经处于计时状态抛出
     */
    public Stopwatch start() {
        Assert.isTrue(!isRunning, "This stopwatch is already running.");
        isRunning = true;
        startTick = ticker.read();
        return this;
    }

    /**
     * 停止当前{@code Stopwatch}实例
     *
     * @return 当前{@code Stopwatch}实例
     * @throws IllegalStateException 如果当前{@code Stopwatch}实例已经处于停止状态时抛出
     */
    public Stopwatch stop() {
        long tick = ticker.read();
        Assert.isTrue(isRunning, "This stopwatch is already stopped.");
        isRunning = false;
        elapsedNanos += tick - startTick;
        return this;
    }

    /**
     * {@code Stopwatch}计次
     *
     * @return 当前{@code Stopwatch}实例
     * @throws IllegalStateException 如果当前{@code Stopwatch}实例已经处于停止状态时抛出
     */
    public Stopwatch record() {
        long tick = ticker.read();
        Assert.isTrue(isRunning, "This stopwatch is topped.");
        if (recordLen == records.length) {
            long[] tmp = new long[recordLen + 100];
            System.arraycopy(records, 0, tmp, 0, recordLen);
            records = tmp;
        }
        records[recordLen++] = elapsedNanos + tick - startTick;
        return this;
    }

    /**
     * 重置当前{@code Stopwatch}实例，停止计时并将相关计时器归零
     *
     * @return 当前{@code Stopwatch}实例
     */
    public Stopwatch reset() {
        elapsedNanos = 0;
        isRunning = false;
        recordLen = 0;
        Arrays.fill(records, 0);
        return this;
    }

    private long elapsedNanos() {
        return isRunning ? ticker.read() - startTick + elapsedNanos : elapsedNanos;
    }

    private long elapsedRecordNanos(int recordIndex) {
        return records[Assert.inclusiveBetween(1, recordLen, recordIndex, "不存在序号为" + recordIndex + "的计次数据") - 1];
    }

    public long elapsedRecord(TimeUnit desiredUnit, int recordIndex) {
        return desiredUnit.convert(elapsedRecordNanos(recordIndex), NANOSECONDS);
    }

    public Duration elapsedRecord(int recordIndex) {
        return Duration.ofNanos(elapsedRecordNanos(recordIndex));
    }

    public long elapsed(TimeUnit desiredUnit) {
        return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
    }

    public Duration elapsed() {
        return Duration.ofNanos(elapsedNanos());
    }

    @Override
    public String toString() {
        long nanos = elapsedNanos();

        TimeUnit unit = chooseUnit(nanos);
        double value = (double) nanos / NANOSECONDS.convert(1, unit);

        return String.format(Locale.ROOT, "%.4g", value) + " " + abbreviate(unit);
    }

    private static TimeUnit chooseUnit(long nanos) {
        if (DAYS.convert(nanos, NANOSECONDS) > 0) {
            return DAYS;
        }
        if (HOURS.convert(nanos, NANOSECONDS) > 0) {
            return HOURS;
        }
        if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
            return MINUTES;
        }
        if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
            return SECONDS;
        }
        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MILLISECONDS;
        }
        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MICROSECONDS;
        }
        return NANOSECONDS;
    }

    private static String abbreviate(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "\u03bcs"; // μs
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new AssertionError();
        }
    }
}
