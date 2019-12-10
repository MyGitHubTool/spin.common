package org.spin.core.function.serializable;

import java.io.Serializable;

@FunctionalInterface
public interface Function<T, R> extends java.util.function.Function<T, R>, Serializable {
}
