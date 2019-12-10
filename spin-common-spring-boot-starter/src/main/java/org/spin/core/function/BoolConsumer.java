package org.spin.core.function;

import org.spin.core.Assert;

/**
 * <p>Created by xuweinan on 2017/10/24.</p>
 *
 * @author xuweinan
 */
@FunctionalInterface
public interface BoolConsumer {

    void accept(boolean value);

    default BoolConsumer andThen(BoolConsumer after) {
        Assert.notNull(after);
        return (boolean t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
