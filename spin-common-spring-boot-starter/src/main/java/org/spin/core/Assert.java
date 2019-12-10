package org.spin.core;

import org.spin.core.throwable.AssertFailException;
import org.spin.core.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * 断言工具
 * <p>异常抛出规则：</p>
 * <ul>
 * <li>断言失败将抛出 {@link AssertFailException}.</li>
 * </ul>
 * <p>线程安全</p>
 */
public abstract class Assert {

    private static final String DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE = "[Assertion failed] - The value %s is not in the specified exclusive range of %s to %s";
    private static final String DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE = "[Assertion failed] - The value %s is not in the specified inclusive range of %s to %s";
    private static final String DEFAULT_MATCHES_PATTERN_EX = "[Assertion failed] - The string %s does not match the pattern %s";
    private static final String DEFAULT_IS_NULL_EX_MESSAGE = "[Assertion failed] - this argument is required; it must not be null";
    private static final String DEFAULT_IS_TRUE_EX_MESSAGE = "[Assertion failed] - this expression must be true";
    private static final String DEFAULT_NO_NULL_ELEMENTS_ARRAY_EX_MESSAGE = "[Assertion failed] - The validated array contains null element at index: %d";
    private static final String DEFAULT_NO_NULL_ELEMENTS_COLLECTION_EX_MESSAGE = "[Assertion failed] - The validated collection contains null element at index: %d";
    private static final String DEFAULT_NOT_BLANK_EX_MESSAGE = "[Assertion failed] - The validated character sequence is blank";
    private static final String DEFAULT_NOT_EMPTY_ARRAY_EX_MESSAGE = "[Assertion failed] - The validated array is empty";
    private static final String DEFAULT_NOT_EMPTY_CHAR_SEQUENCE_EX_MESSAGE = "[Assertion failed] - The validated character sequence is empty";
    private static final String DEFAULT_NOT_EMPTY_COLLECTION_EX_MESSAGE = "[Assertion failed] - The validated collection is empty";
    private static final String DEFAULT_NOT_EMPTY_MAP_EX_MESSAGE = "[Assertion failed] - The validated map is empty";
    private static final String DEFAULT_VALID_INDEX_ARRAY_EX_MESSAGE = "[Assertion failed] - The validated array index is invalid: %d";
    private static final String DEFAULT_VALID_INDEX_CHAR_SEQUENCE_EX_MESSAGE = "[Assertion failed] - The validated character sequence index is invalid: %d";
    private static final String DEFAULT_VALID_INDEX_COLLECTION_EX_MESSAGE = "[Assertion failed] - The validated collection index is invalid: %d";
    private static final String DEFAULT_VALID_STATE_EX_MESSAGE = "[Assertion failed] - The validated state must be true";
    private static final String DEFAULT_IS_ASSIGNABLE_EX_MESSAGE = "[Assertion failed] - Cannot assign a %s to a %s";
    private static final String DEFAULT_IS_INSTANCE_OF_EX_MESSAGE = "[Assertion failed] - Expected type: %s, actual: %s";

    private Assert() {
    }
    // isTrue
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则抛出指定的异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0: &#37;d", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param exception  条件不成立时的异常, 不能为空
     */
    public static void isTrue(final boolean expression, Supplier<? extends RuntimeException> exception) {
        if (!expression) {
            throw exception.get();
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0: &#37;d", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件不成立时的异常信息, 不能为空
     * @param value      条件不成立时，追加在异常信息中的long值
     * @throws AssertFailException 条件不成立时抛出异常 {@code false}
     * @see #isTrue(boolean)
     * @see #isTrue(boolean, String, double)
     * @see #isTrue(boolean, String, Object...)
     */
    public static void isTrue(final boolean expression, final String message, final long value) {
        if (!expression) {
            throw new AssertFailException(String.format(message, value));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0: &#37;s", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件不成立时的异常信息, 不能为空
     * @param value      条件不成立时，追加在异常信息中的double值
     * @throws AssertFailException 条件不成立时抛出异常 {@code false}
     * @see #isTrue(boolean)
     * @see #isTrue(boolean, String, long)
     * @see #isTrue(boolean, String, Object...)
     */
    public static void isTrue(final boolean expression, final String message, final double value) {
        if (!expression) {
            throw new AssertFailException(String.format(message, value));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0: &#37;d, &#37;s", i, 0.0);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件不成立时的异常信息, 不能为空
     * @param values     条件不成立时，追加在异常信息中的参数
     * @throws AssertFailException 条件不成立时抛出异常 {@code false}
     * @see #isTrue(boolean)
     * @see #isTrue(boolean, String, long)
     * @see #isTrue(boolean, String, double)
     */
    public static void isTrue(final boolean expression, final String message, final Object... values) {
        if (!expression) {
            throw new AssertFailException(String.format(message, values));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code true}; 否则抛出异常
     * <pre>Assert.notTrue(i &gt; 0, "必须大于0");</pre>
     *
     * @param expression 需要判断的bool表达式
     * @throws AssertFailException 条件不成立时抛出异常 {@code false}
     * @see #isTrue(boolean, String, long)
     * @see #isTrue(boolean, String, double)
     * @see #isTrue(boolean, String, Object...)
     */
    public static void isTrue(final boolean expression) {
        if (!expression) {
            throw new AssertFailException(DEFAULT_IS_TRUE_EX_MESSAGE);
        }
    }

    // notTrue
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则抛出指定的异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0: &#37;d", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param exception  条件成立时的异常, 不能为空
     * @see #notTrue(boolean)
     * @see #notTrue(boolean, String, double)
     * @see #notTrue(boolean, String, Object...)
     */
    public static void notTrue(final boolean expression, Supplier<? extends RuntimeException> exception) {
        if (expression) {
            throw exception.get();
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0: &#37;d", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件成立时的异常信息, 不能为空
     * @param value      条件成立时，追加在异常信息中的long值
     * @throws AssertFailException 条件成立时抛出异常 {@code true}
     * @see #notTrue(boolean)
     * @see #notTrue(boolean, String, double)
     * @see #notTrue(boolean, String, Object...)
     */
    public static void notTrue(final boolean expression, final String message, final long value) {
        if (expression) {
            throw new AssertFailException(String.format(message, value));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0: &#37;s", i);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件成立时的异常信息, 不能为空
     * @param value      条件成立时，追加在异常信息中的double值
     * @throws AssertFailException 条件成立时抛出异常 {@code true}
     * @see #notTrue(boolean)
     * @see #notTrue(boolean, String, long)
     * @see #notTrue(boolean, String, Object...)
     */
    public static void notTrue(final boolean expression, final String message, final double value) {
        if (expression) {
            throw new AssertFailException(String.format(message, value));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则使用指定的消息抛出异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0: &#37;d, &#37;s", i, 0.0);</pre>
     *
     * @param expression 需要判断的bool表达式
     * @param message    {@link String#format(String, Object...)} 条件成立时的异常信息, 不能为空
     * @param values     条件成立时，追加在异常信息中的参数
     * @throws AssertFailException 条件成立时抛出异常 {@code true}
     * @see #notTrue(boolean)
     * @see #notTrue(boolean, String, long)
     * @see #notTrue(boolean, String, double)
     */
    public static void notTrue(final boolean expression, final String message, final Object... values) {
        if (expression) {
            throw new AssertFailException(String.format(message, values));
        }
    }

    /**
     * 断言指定的bool表达式结果为 {@code false}; 否则抛出异常
     * <pre>Assert.notTrue(i &gt; 0.0, "必须小于等于0");</pre>
     *
     * @param expression 需要判断的bool表达式
     * @throws AssertFailException 条件成立时抛出异常 {@code true}
     * @see #notTrue(boolean, String, long)
     * @see #notTrue(boolean, String, double)
     * @see #notTrue(boolean, String, Object...)
     */
    public static void notTrue(final boolean expression) {
        if (expression) {
            throw new AssertFailException(DEFAULT_IS_TRUE_EX_MESSAGE);
        }
    }

    // notNull
    //---------------------------------------------------------------------------------

    /**
     * 断言指定对象不为 {@code null}，并返回该对象。否则抛出异常
     * <pre>Assert.notNull(myObject);</pre>
     *
     * <p>异常信息为:  &quot;[Assertion failed] - this argument is required; it must not be null&quot;.</p>
     *
     * @param <T>    类型参数
     * @param object 待检查对象
     * @return 返回对象自身 (一定不为 {@code null})
     * @throws AssertFailException 当对象为空时抛出 {@code null}
     * @see #notNull(Object, String, Object...)
     */
    public static <T> T notNull(final T object) {
        return notNull(object, DEFAULT_IS_NULL_EX_MESSAGE);
    }

    /**
     * 断言指定对象不为 {@code null}，并返回该对象。否则抛出异常
     * <pre>Assert.notNull(myObject, "对象不能为空");</pre>
     *
     * @param <T>     类型参数
     * @param object  待检查对象
     * @param message 对象为null时的异常信息 {@link String#format(String, Object...)}，不能为空
     * @param values  异常信息的填充参数
     * @return 返回对象自身 (一定不为 {@code null})
     * @throws AssertFailException 当对象为空时抛出 {@code null}
     * @see #notNull(Object)
     */
    public static <T> T notNull(final T object, final String message, final Object... values) {
        if (object == null) {
            throw new AssertFailException(String.format(message, values));
        }
        return object;
    }

    // notEmpty array
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的数组不为null，且长度&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myArray, "The array must not be empty");</pre>
     *
     * @param <T>     数组元素类型
     * @param array   待检查的数组
     * @param message 异常信息 {@link String#format(String, Object...)}，不能为空
     * @param values  异常信息的填充参数
     * @return 检查通过时返回原数组
     * @throws AssertFailException 当数组为{@code null}时抛出
     * @throws AssertFailException 当数组为空时抛出
     */
    public static <T> T[] notEmpty(final T[] array, final String message, final Object... values) {
        if (array == null) {
            throw new AssertFailException(String.format(message, values));
        }
        if (array.length == 0) {
            throw new AssertFailException(String.format(message, values));
        }
        return array;
    }

    /**
     * 断言指定的数组不为null，且长度&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myArray);</pre>
     *
     * <p>异常信息为 &quot;The validated array is
     * empty&quot;.</p>
     *
     * @param <T>   数组元素类型
     * @param array 待检查的数组
     * @return 检查通过时返回原数组
     * @throws AssertFailException 当数组为{@code null}时抛出
     * @throws AssertFailException 当数组为空时抛出
     */
    public static <T> T[] notEmpty(final T[] array) {
        return notEmpty(array, DEFAULT_NOT_EMPTY_ARRAY_EX_MESSAGE);
    }

    // notEmpty collection
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的集合不为null，且size&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myCollection, "The collection must not be empty");</pre>
     *
     * @param <T>        集合参数类型
     * @param collection 待检查的集合
     * @param message    异常信息 {@link String#format(String, Object...)}，不能为空
     * @param values     异常信息的填充参数
     * @return 检查通过时返回原集合
     * @throws AssertFailException 当集合为{@code null}时抛出
     * @throws AssertFailException 当集合为空时抛出
     */
    public static <T extends Collection<?>> T notEmpty(final T collection, final String message, final Object... values) {
        if (collection == null) {
            throw new AssertFailException(String.format(message, values));
        }
        if (collection.isEmpty()) {
            throw new AssertFailException(String.format(message, values));
        }
        return collection;
    }

    /**
     * 断言指定的集合不为null，且size&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myCollection);</pre>
     *
     * <p>异常信息为 &quot;[Assertion failed] - The validated collection is
     * empty&quot;.</p>
     *
     * @param <T>        集合参数类型
     * @param collection 待检查的集合
     * @return 检查通过时返回原集合
     * @throws AssertFailException 当集合为{@code null}时抛出
     * @throws AssertFailException 当集合为空时抛出
     */
    public static <T extends Collection<?>> T notEmpty(final T collection) {
        return notEmpty(collection, DEFAULT_NOT_EMPTY_COLLECTION_EX_MESSAGE);
    }

    // notEmpty map
    //---------------------------------------------------------------------------------

    /**
     * 断言指定的Map不为null，且size&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myMap, "The map must not be empty");</pre>
     *
     * @param <T>     Map参数类型
     * @param map     待检查的Map
     * @param message 异常信息 {@link String#format(String, Object...)}，不能为空
     * @param values  异常信息的填充参数
     * @return 检查通过时返回原Map
     * @throws AssertFailException 当Map为{@code null}时抛出
     * @throws AssertFailException 当Map为空时抛出
     */
    public static <T extends Map<?, ?>> T notEmpty(final T map, final String message, final Object... values) {
        if (map == null) {
            throw new AssertFailException(String.format(message, values));
        }
        if (map.isEmpty()) {
            throw new AssertFailException(String.format(message, values));
        }
        return map;
    }

    /**
     * 断言指定的Map不为null，且size&gt;0（一定有元素），否则抛出异常
     * <pre>Assert.notEmpty(myMap);</pre>
     *
     * <p>异常信息为 &quot;The validated map is
     * empty&quot;.</p>
     *
     * @param <T> Map参数类型
     * @param map 待检查的Map
     * @return 检查通过时返回原Map
     * @throws AssertFailException 当Map为{@code null}时抛出
     * @throws AssertFailException 当Map为空时抛出
     */
    public static <T extends Map<?, ?>> T notEmpty(final T map) {
        return notEmpty(map, DEFAULT_NOT_EMPTY_MAP_EX_MESSAGE);
    }

    // notEmpty string
    //---------------------------------------------------------------------------------

    /**
     * <p>断言指定的字符串一定非空（不为null且长度大于0）
     * </p>
     * <pre>Assert.notEmpty(myString, "The string must not be empty");</pre>
     *
     * @param <T>     字符串类型
     * @param chars   待检查的字符串
     * @param message {@link String#format(String, Object...)} 检查失败时的信息
     * @param values  填充失败信息的参数
     * @return 检查通过时返回原字符串
     * @throws AssertFailException 当字符串为{@code null} 或长度为0时抛出
     */
    public static <T extends CharSequence> T notEmpty(final T chars, final String message, final Object... values) {
        if (chars == null) {
            throw new AssertFailException(String.format(message, values));
        }
        if (chars.length() == 0) {
            throw new AssertFailException(String.format(message, values));
        }
        return chars;
    }

    public static <T extends CharSequence> T notEmpty(final T chars) {
        return notEmpty(chars, DEFAULT_NOT_EMPTY_CHAR_SEQUENCE_EX_MESSAGE);
    }

    // validIndex array
    //---------------------------------------------------------------------------------

    public static <T> T[] validIndex(final T[] array, final int index, final String message, final Object... values) {
        Assert.notNull(array);
        if (index < 0 || index >= array.length) {
            throw new AssertFailException(String.format(message, values));
        }
        return array;
    }

    public static <T> T[] validIndex(final T[] array, final int index) {
        return validIndex(array, index, DEFAULT_VALID_INDEX_ARRAY_EX_MESSAGE, index);
    }

    /**
     * 验证数组中没有null元素
     *
     * @param array 待判断数组
     * @param <T>   数组元素类型
     * @return 原数组
     */
    public static <T> T[] doesNotContainsNull(final T[] array) {
        Assert.notNull(array);
        for (int i = 0; i < array.length; i++) {
            if (null == array[i]) {
                throw new AssertFailException(String.format(DEFAULT_NO_NULL_ELEMENTS_ARRAY_EX_MESSAGE, i));
            }
        }
        return array;
    }

    // validIndex collection
    //---------------------------------------------------------------------------------

    public static <T extends Collection<?>> T validIndex(final T collection, final int index, final String message, final Object... values) {
        Assert.notNull(collection);
        if (index < 0 || index >= collection.size()) {
            throw new AssertFailException(String.format(message, values));
        }
        return collection;
    }

    public static <T extends Collection<?>> T validIndex(final T collection, final int index) {
        return validIndex(collection, index, DEFAULT_VALID_INDEX_COLLECTION_EX_MESSAGE, index);
    }

    /**
     * 验证集合中没有null元素
     *
     * @param collection 待判断集合
     * @param <T>        集合类型
     * @return 原集合
     */
    public static <T extends Collection<?>> T doesNotContainsNull(final T collection) {
        Assert.notNull(collection);

        int i = 0;
        for (Object o : collection) {
            if (null == o) {
                throw new AssertFailException(String.format(DEFAULT_NO_NULL_ELEMENTS_COLLECTION_EX_MESSAGE, i));
            }
            ++i;
        }
        return collection;
    }

    // validIndex string
    //---------------------------------------------------------------------------------

    public static <T extends CharSequence> T validIndex(final T chars, final int index, final String message, final Object... values) {
        Assert.notNull(chars);
        if (index < 0 || index >= chars.length()) {
            throw new AssertFailException(String.format(message, values));
        }
        return chars;
    }

    public static <T extends CharSequence> T validIndex(final T chars, final int index) {
        return validIndex(chars, index, DEFAULT_VALID_INDEX_CHAR_SEQUENCE_EX_MESSAGE, index);
    }

    public static String notBlank(String text, String message) {
        if (StringUtils.isBlank(text)) {
            throw new AssertFailException(message);
        }
        return text;
    }

    public static String notBlank(String text) {
        return notBlank(text, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");

    }

    public static String doesNotContain(String textToSearch, String substring, String message) {
        if (StringUtils.isNotEmpty(textToSearch) && StringUtils.isNotEmpty(substring) &&
            textToSearch.contains(substring)) {
            throw new AssertFailException(message);
        }
        return textToSearch;
    }

    public static String doesNotContain(String textToSearch, String substring) {
        return doesNotContain(textToSearch, substring,
            "[Assertion failed] - this String argument must not contain the substring [" + substring + "]");
    }

    // matchesPattern
    //---------------------------------------------------------------------------------

    public static <T extends CharSequence> T matchesPattern(final T input, final String pattern) {
        if (!Pattern.matches(pattern, input)) {
            throw new AssertFailException(String.format(DEFAULT_MATCHES_PATTERN_EX, input, pattern));
        }
        return input;
    }

    public static <T extends CharSequence> T matchesPattern(final T input, final String pattern, final String message, final Object... values) {
        if (!Pattern.matches(pattern, input)) {
            throw new AssertFailException(String.format(message, values));
        }
        return input;
    }

    // inclusiveBetween
    //---------------------------------------------------------------------------------

    public static <T extends Comparable<T>> T inclusiveBetween(final T start, final T end, final T value) {
        if (value.compareTo(start) < 0 || value.compareTo(end) > 0) {
            throw new AssertFailException(String.format(DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    public static <T extends Comparable<T>> T inclusiveBetween(final T start, final T end, final T value, final String message, final Object... values) {
        if (value.compareTo(start) < 0 || value.compareTo(end) > 0) {
            throw new AssertFailException(String.format(message, values));
        }
        return value;
    }

    public static int inclusiveBetween(final int start, final int end, final int value) {
        if (value < start || value > end) {
            throw new AssertFailException(String.format(DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    public static int inclusiveBetween(final int start, final int end, final int value, final String message) {
        if (value < start || value > end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    public static long inclusiveBetween(final long start, final long end, final long value) {
        if (value < start || value > end) {
            throw new AssertFailException(String.format(DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    public static long inclusiveBetween(final long start, final long end, final long value, final String message) {
        if (value < start || value > end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    public static double inclusiveBetween(final double start, final double end, final double value) {
        if (value < start || value > end) {
            throw new AssertFailException(String.format(DEFAULT_INCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    public static double inclusiveBetween(final double start, final double end, final double value, final String message) {
        if (value < start || value > end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    // exclusiveBetween
    //---------------------------------------------------------------------------------

    public static <T extends Comparable<T>> T exclusiveBetween(final T start, final T end, final T value) {
        if (value.compareTo(start) <= 0 || value.compareTo(end) >= 0) {
            throw new AssertFailException(String.format(DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    public static <T extends Comparable<T>> T exclusiveBetween(final T start, final T end, final T value, final String message, final Object... values) {
        if (value.compareTo(start) <= 0 || value.compareTo(end) >= 0) {
            throw new AssertFailException(String.format(message, values));
        }
        return value;
    }

    public static int exclusiveBetween(final int start, final int end, final int value) {
        if (value <= start || value >= end) {
            throw new AssertFailException(String.format(DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    public static int exclusiveBetween(final int start, final int end, final int value, final String message) {
        if (value <= start || value >= end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    public static long exclusiveBetween(final long start, final long end, final long value) {
        if (value <= start || value >= end) {
            throw new AssertFailException(String.format(DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    public static long exclusiveBetween(final long start, final long end, final long value, final String message) {
        if (value <= start || value >= end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    public static double exclusiveBetween(final double start, final double end, final double value) {
        if (value <= start || value >= end) {
            throw new AssertFailException(String.format(DEFAULT_EXCLUSIVE_BETWEEN_EX_MESSAGE, value, start, end));
        }
        return value;
    }

    public static double exclusiveBetween(final double start, final double end, final double value, final String message) {
        if (value <= start || value >= end) {
            throw new AssertFailException(message);
        }
        return value;
    }

    // isInstanceOf
    //---------------------------------------------------------------------------------

    public static <T> T isInstanceOf(final Class<T> type, final Object obj) {
        return isInstanceOf(type, obj, DEFAULT_IS_INSTANCE_OF_EX_MESSAGE, type.getName());
    }

    public static <T> T isInstanceOf(final Class<?> type, final Object obj, final String message, final Object... values) {
        if (!notNull(type).isInstance(obj)) {
            throw new AssertFailException(String.format(message, values));
        }
        //noinspection unchecked
        return (T) obj;
    }

    // isAssignableFrom
    //---------------------------------------------------------------------------------

    public static <T> Class<T> isAssignableFrom(final Class<?> superType, final Class<T> type) {
        return isAssignableFrom(superType, type, DEFAULT_IS_ASSIGNABLE_EX_MESSAGE, type.getName(),
            superType.getName());
    }

    public static <T> Class<T> isAssignableFrom(final Class<?> superType, final Class<T> type, final String message, final Object... values) {
        if (!superType.isAssignableFrom(type)) {
            throw new AssertFailException(String.format(message, values));
        }
        return type;
    }
}

