package org.spin.core.function;

import org.spin.core.Assert;

/**
 * <p>Created by xuweinan on 2017/10/24.</p>
 *
 * @author xuweinan
 */
@FunctionalInterface
public interface ShortConsumer {

    void accept(short value);

    default ShortConsumer andThen(ShortConsumer after) {
        Assert.notNull(after);
        return (short t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
