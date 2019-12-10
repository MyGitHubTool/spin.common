package org.spin.core.function.serializable;

import java.io.Serializable;

@FunctionalInterface
public interface BiConsumer<T, U> extends java.util.function.BiConsumer<T, U>, Serializable {
}
