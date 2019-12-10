package org.spin.core.function;

import org.spin.core.Assert;

/**
 * <p>Created by xuweinan on 2017/10/24.</p>
 *
 * @author xuweinan
 */
@FunctionalInterface
public interface CharConsumer {

    void accept(char value);

    default CharConsumer andThen(CharConsumer after) {
        Assert.notNull(after);
        return (char t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
