package org.spin.core.base;

@FunctionalInterface
public interface Ticker {
    long read();

    Ticker SYSTEM_TICKER = System::nanoTime;
}
