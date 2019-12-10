package org.spin.core.function;

import org.spin.core.Assert;
import org.spin.core.trait.Order;

/**
 * 无参数，无返回值的handler
 * <p>Created by xuweinan on 2017/10/24.</p>
 *
 * @author xuweinan
 */
@FunctionalInterface
public interface Handler extends Order {
    void handle();

    default Handler andThen(Handler after) {
        Assert.notNull(after);
        return () -> {
            handle();
            after.handle();
        };
    }
}
