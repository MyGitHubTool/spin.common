package org.spin.core.function;

import org.spin.core.Assert;

/**
 * <p>Created by xuweinan on 2017/10/24.</p>
 *
 * @author xuweinan
 */
@FunctionalInterface
public interface FloatConsumer {

    void accept(float value);

    default FloatConsumer andThen(FloatConsumer after) {
        Assert.notNull(after);
        return (float t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
