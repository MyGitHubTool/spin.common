package org.spin.core.function;

import org.spin.core.Assert;

/**
 * <p>Created by xuweinan on 2017/10/24.</p>
 *
 * @author xuweinan
 */
@FunctionalInterface
public interface ByteConsumer {

    void accept(byte value);

    default ByteConsumer andThen(ByteConsumer after) {
        Assert.notNull(after);
        return (byte t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
