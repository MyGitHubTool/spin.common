package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.collection.Tuple;
import org.spin.core.function.serializable.BiConsumer;
import org.spin.core.function.serializable.Function;
import org.spin.core.throwable.SpinException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Bean工具类
 * Created by xuweinan on 2016/8/15.
 */
public abstract class BeanUtils {
    private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);
    private static final Map<String, Map<String, PropertyDescriptorWrapper>> CLASS_PROPERTY_CACHE = new ConcurrentHashMap<>();

    private BeanUtils() {
    }

    public static Method tailMethod(Class<?> type, String name) {
        try {
            return type.getMethod(name, String.class, Object.class);
        } catch (NoSuchMethodException | SecurityException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * 通过类的默认构造方法创建一个实例
     *
     * @param className 类名
     * @param <T>       类类型
     * @return T的实例
     */
    public static <T> T instantiateClass(String className) {
        Assert.notEmpty(className, "Class Name must not be null");
        Class<T> clazz;
        try {
            //noinspection unchecked
            clazz = (Class<T>) ClassUtils.getClass(className);
        } catch (ClassNotFoundException e) {
            throw new SpinException("未找到类:" + className);
        } catch (Exception e) {
            throw new SpinException("类型不匹配" + className);
        }
        return instantiateClass(clazz);
    }

    /**
     * 通过类的默认构造方法创建一个实例
     *
     * @param clazz 类
     * @param <T>   类类型
     * @return T的实例
     */
    public static <T> T instantiateClass(Class<T> clazz) {
        if (Assert.notNull(clazz, "Class must not be null").isInterface()) {
            throw new SpinException(clazz.getName() + " is an interface");
        }
        try {
            return instantiateClass(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException ex) {
            throw new SpinException("No default constructor found", ex);
        }
    }

    /**
     * 调用指定构造方法创建一个实例
     *
     * @param ctor 构造方法
     * @param <T>  类类型
     * @param args 参数
     * @return T的实例
     */
    public static <T> T instantiateClass(Constructor<T> ctor, Object... args) {
        Assert.notNull(ctor, "Constructor must not be null");
        try {
            ReflectionUtils.makeAccessible(ctor);
            return ctor.newInstance(args);
        } catch (InstantiationException ex) {
            throw new SpinException("Is " + ctor.getName() + " an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new SpinException("Is the constructor " + ctor.getName() + " accessible?", ex);
        } catch (IllegalArgumentException ex) {
            throw new SpinException("Illegal arguments for constructor " + ctor.getName(), ex);
        } catch (InvocationTargetException ex) {
            throw new SpinException("Constructor " + ctor.getName() + " threw exception", ex.getTargetException());
        }
    }

    /**
     * 将properties中的属性通过setter设置到bean中，bean中不存在的属性将被忽略
     *
     * @param bean       目标对象
     * @param properties 属性properties
     */
    public static void applyProperties(Object bean, Map<?, ?> properties) {
        properties.forEach((key, value) -> {
            String getterName = "get" + StringUtils.capitalize(key.toString());
            String setterName = "set" + StringUtils.capitalize(key.toString());
            try {
                Object v = value;
                Class<?>[] args = {};
                Method getter = MethodUtils.getAccessibleMethod(bean.getClass(), getterName, args);
                if (Objects.nonNull(getter)) {
                    v = ObjectUtils.convert(getter.getReturnType(), value);
                }
                MethodUtils.invokeMethod(bean, setterName, v);
            } catch (NoSuchMethodException e) {
                logger.info("不存在属性[" + key + "]的set方法");
            } catch (IllegalAccessException e) {
                throw new SpinException("属性[" + key + "]的set方法不允许访问");
            } catch (InvocationTargetException e) {
                throw new SpinException("设置属性[" + key + "]失败", e);
            }
        });
    }

    /**
     * 将平面的Map转换成树状组织的Map
     *
     * @param values 扁平的Map
     * @return 树状Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> wrapperFlatMap(Map<String, Object> values) {
        Map<String, Object> treeSightMap = new HashMap<>();
        int off;
        int next;
        int depth;
        String p;
        String[] propName = new String[100];
        Map<String, Object> work;
        Map<String, Object> bak;
        Map<String, Object> tmp;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            off = 0;
            depth = 0;
            p = entry.getKey();
            while ((next = p.indexOf('.', off)) != -1) {
                propName[depth++] = p.substring(off, next);
                off = next + 1;
            }
            propName[depth++] = (p.substring(off));
            if (depth == 1) {
                treeSightMap.put(propName[0], entry.getValue());
                continue;
            }
            work = treeSightMap;
            int i = 0;
            while (depth != i) {
                if (i != depth - 1) {
                    tmp = (Map<String, Object>) work.get(propName[i]);
                    if (tmp != null) {
                        work = tmp;
                        ++i;
                        continue;
                    }
                    bak = new HashMap<>();
                    work.put(propName[i], bak);
                    work = bak;
                    ++i;
                } else {
                    work.put(propName[i], entry.getValue());
                    ++i;
                }
            }
        }
        return treeSightMap;
    }

    /**
     * 通过getter或者setter方法的名称，获取field名称
     *
     * @param getterOrSetter getter/setter方法名称
     * @return field名称
     */
    public static String toFieldName(String getterOrSetter) {
        if (null == getterOrSetter || getterOrSetter.length() < 2) {
            return getterOrSetter;
        }
        if (getterOrSetter.startsWith("get") || getterOrSetter.startsWith("set")) {
            return StringUtils.uncapitalize(getterOrSetter.substring(3));
        }
        if (getterOrSetter.startsWith("is")) {
            return StringUtils.uncapitalize(getterOrSetter.substring(2));
        }
        return getterOrSetter;
    }

    /**
     * 获取对象指定属性的值
     * <p>通过反射直接读取属性, 如果字段不存在, 则会查找get方法.
     * 数组, {@link Iterable}, {@link Tuple}等可迭代类型通过"[idx]"索引位置访问，Map中的元素可以直接访问，如果需要访问{@link Map}对象中的成员变量,
     * 需要在变量名前加"#", 如map.#size</p>
     * <p>获取数组与List等可迭代类型中的第n个元素：list[n], 高维数组(嵌套集合): list[x][y][z]</p>
     * <p>获取中Map中键为key对应的value：map.key</p>
     * <p>获取中Map中名称为size的成员变量的值：map.#size</p>
     *
     * @param target    对象实例
     * @param valuePath 属性名称，支持嵌套
     * @param <T>       属性类型参数
     * @return 属性值
     */
    public static <T> T getFieldValue(Object target, String valuePath) {
        String[] valuePaths = Assert.notEmpty(valuePath, "valuePath必须指定属性名称").split("\\.");
        if (null == target) {
            return null;
        }
        Object o = target;
        for (int i = 0; i < valuePaths.length; i++) {
            String field = valuePaths[i];
            List<Integer> seqs = new LinkedList<>();
            if (field.indexOf('[') != -1) {
                final StringBuilder f = new StringBuilder(field.length());
                final StringBuilder seq = new StringBuilder(field.length());
                boolean inPos = false;
                for (char c : field.toCharArray()) {
                    switch (c) {
                        case '[':
                            if (inPos) {
                                throw new SpinException("索引表达式未正确结束: " + field);
                            }
                            inPos = true;
                            break;
                        case ']':
                            if (!inPos) {
                                throw new SpinException("索引表达式未正确开始: " + field);
                            }
                            try {
                                seqs.add(Integer.parseInt(seq.toString()));
                            } catch (NumberFormatException ignore) {
                                throw new SpinException("索引必须是合法的数字: " + field);
                            }
                            seq.setLength(0);
                            inPos = false;
                            break;
                        case '-':
                            if (inPos) {
                                throw new SpinException("索引不能为负数: " + field);
                            }
                        default:
                            if (inPos) {
                                seq.append(c);
                            } else {
                                f.append(c);
                            }
                            break;
                    }
                }
                field = f.toString();
                if (i != 0 && f.length() == 0) {
                    throw new SpinException("表达式不合法，未指定索引的对象" + field);
                }
            }

            if (field.length() > 0) {
                char mark = field.charAt(0);

                if (i < valuePath.length() - 1 && null == o) {
                    throw new SpinException(field + "属性为null");
                }
                if ('#' != mark && o instanceof Map) {
                    o = ((Map) o).get(field);
                } else {
                    Field f;
                    if (mark == '#') {
                        field = field.substring(1);
                    }
                    try {
                        f = ReflectionUtils.findField(o.getClass(), field);
                        ReflectionUtils.makeAccessible(f);
                        o = ReflectionUtils.getField(f, o);
                    } catch (Exception e) {
                        String getterName = "get" + StringUtils.capitalize(field);
                        try {
                            o = MethodUtils.invokeMethod(o, getterName, null);
                        } catch (Exception ex) {
                            throw new SpinException(o.getClass().toString() + "不存在" + field + "属性", ex);
                        }
                    }
                }
            }

            for (Integer idx : seqs) {
                if (null == o) {
                    throw new SpinException(field + "属性为null");
                }

                if (o instanceof List) {
                    if (((List) o).size() <= idx) {
                        throw new SpinException(idx + " 索引超出范围0-" + ((List) o).size());
                    }
                    o = ((List) o).get(idx);
                } else if (o.getClass().isArray()) {
                    @SuppressWarnings("ConstantConditions")
                    Object[] t = (Object[]) o;
                    if (t.length <= idx) {
                        throw new SpinException(idx + " 索引超出范围0-" + t.length);
                    }
                    o = t[idx];
                } else if (o instanceof Collection) {
                    Collection<?> t = (Collection<?>) o;
                    if (t.size() <= idx) {
                        throw new SpinException(idx + " 索引超出范围0-" + t.size());
                    }
                    int k = 0;
                    for (Object obj : t) {
                        if (k == idx) {
                            o = obj;
                            break;
                        }
                        ++k;
                    }
                } else if (o instanceof Tuple) {
                    Tuple<?> t = (Tuple<?>) o;

                    if (t.size() <= idx) {
                        throw new SpinException(idx + " 索引超出范围0-" + t.size());
                    }
                    o = t.get(idx);
                } else if (o instanceof Iterable) {
                    Iterable<?> t = (Iterable<?>) o;

                    int k = 0;
                    for (Object obj : t) {
                        if (k == idx) {
                            o = obj;
                            break;
                        }
                        ++k;
                    }
                    if (k <= idx) {
                        throw new SpinException(idx + " 索引超出范围0-" + k);
                    }
                } else {
                    throw new SpinException(o.getClass().toString() + "不支持索引方式访问");
                }
            }
        }
        //noinspection unchecked
        return (T) o;
    }

    /**
     * 获取对象指定属性的值
     *
     * @param target 对象实例
     * @param fields 取值的属性名称
     * @return 属性map
     */
    public static Map<String, Object> getFieldValue(Object target, String... fields) {
        return getFieldValue(target, Arrays.asList(fields));

    }

    /**
     * 获取对象指定属性的值
     *
     * @param target 对象实例
     * @param fields 取值的属性名称
     * @return 属性map
     */
    public static Map<String, Object> getFieldValue(Object target, Collection<String> fields) {
        Map<String, Object> result = new HashMap<>();
        if (CollectionUtils.isEmpty(fields)) {
            return new HashMap<>(0);
        }
        for (String field : fields) {
            Object value = getFieldValue(target, field);
            result.put(field, value);
        }
        return result;
    }

    /**
     * JavaBean转换为Map
     *
     * @param target            bean
     * @param camelToUnderscore 是否将驼峰命名转换为下划线命名方式
     * @return map
     */
    public static Map<String, String> toStringMap(Object target, boolean camelToUnderscore) {
        // null值，直接返回
        if (null == target) {
            return null;
        }

        // 如果是Map，做适应性调整
        if (target instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) target;
            return m.entrySet().stream().filter(entry -> null != entry.getValue()).collect(Collectors.toMap(e -> camelToUnderscore ? StringUtils.underscore(e.getKey().toString()) : e.getKey().toString(), e -> StringUtils.toString(e.getValue())));
        }

        if (isJavaBean(target)) {
            Collection<PropertyDescriptorWrapper> props = getBeanPropertyDes(target.getClass(), true, false).values();
            Map<String, String> res = new HashMap<>(props.size());
            for (PropertyDescriptorWrapper prop : props) {
                try {
                    Object value = prop.reader.invoke(target);
                    if (null != value) {
                        res.put(camelToUnderscore ? StringUtils.underscore(prop.getDescriptor().getName()) : prop.getDescriptor().getName(), StringUtils.toString(value));
                    }
                } catch (IllegalAccessException | InvocationTargetException ignore) {
                    // 忽略访问失败的属性
                }
            }
            return res;
        }

        throw new SpinException(target.getClass().getName() + "不能转换为Map<String, String>");
    }

    /**
     * JavaBean转换为Map(字段名转换为下划线形式)
     *
     * @param target bean
     * @return map
     */
    public static Map<String, String> toUnderscoreStringMap(Object target) {
        return toStringMap(target, true);
    }

    /**
     * JavaBean转换为Map
     *
     * @param target bean
     * @return map
     */
    public static Map<String, String> toStringMap(Object target) {
        return toStringMap(target, false);
    }

    /**
     * JavaBean转换为Map
     *
     * @param target      bean
     * @param recursively 是否递归处理所有属性
     * @return map
     */
    public static Map<String, Object> toMap(Object target, boolean recursively) {
        Object result = toMapInternal(target, recursively);
        if (result instanceof Map || null == result) {
            //noinspection unchecked
            return (Map<String, Object>) result;
        } else {
            throw new SpinException(target.getClass().getName() + "不能被转换为Map");
        }
    }


    private static Object toMapInternal(Object target, boolean recursively) {
        // null值，直接返回
        if (null == target) {
            return null;
        }

        // 如果是Map，做适应性调整
        if (target instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) target;
            if (recursively) {
                return m.entrySet().stream().filter(entry -> null != entry.getValue()).collect(Collectors.toMap(e -> e.getKey().toString(), e -> BeanUtils.toMapInternal(e.getValue(), true)));
            } else {
                return m.entrySet().stream().filter(entry -> null != entry.getValue()).collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
            }
        }

        // 如果是集合，将其元素转为Map后返回
        if (CollectionUtils.isCollection(target)) {
            List objects = CollectionUtils.asList(target);
            for (int i = 0; i < objects.size(); i++) {
                //noinspection unchecked
                objects.set(i, toMapInternal(objects.get(i), recursively));
            }
            return objects;
        }

        // 如果是JavaBean，将其转换为Map
        if (isJavaBean(target)) {
            Collection<PropertyDescriptorWrapper> props = getBeanPropertyDes(target.getClass(), true, false).values();
            Map<String, Object> res = new HashMap<>(props.size());
            for (PropertyDescriptorWrapper prop : props) {
                try {
                    Object value = prop.reader.invoke(target);
                    if (null != value) {
                        res.put(prop.getDescriptor().getName(), toMapInternal(value, recursively));
                    }
                } catch (IllegalAccessException | InvocationTargetException ignore) {
                    // 忽略访问失败的属性
                }
            }
            return res;
        } else {
            // 否则原样返回
            return target;
        }

    }

    /**
     * 判断一个对象是否是JavaBean
     * <p>一个JavaBean，一定是一个自定义对象（非java自带的类，数组，集合，Map，枚举，字符序列，流，异常)</p>
     *
     * @param target 对象
     * @return 是否是JavaBean
     */
    public static boolean isJavaBean(Object target) {
        return !(null == target || target.getClass().isArray()
            || target.getClass().getName().startsWith("java.")
            || target.getClass().getName().startsWith("javax.")
            || target instanceof Map
            || target instanceof Iterable
            || target instanceof Enum
            || target instanceof CharSequence
            || target instanceof Throwable
            || target instanceof AutoCloseable
            || target instanceof Readable
        );
    }

    /**
     * 通过内省机制，获取一个JavaBean的所有属性(必须可写)
     *
     * @param type JavaBean类型
     * @return 属性描述器数组
     */
    public static Map<String, PropertyDescriptorWrapper> getBeanPropertyDes(Class<?> type) {
        Map<String, PropertyDescriptorWrapper> props = CLASS_PROPERTY_CACHE.get(type.getName());
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            props = new HashMap<>();
            Method writer;
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                writer = descriptor.getWriteMethod();
                if (writer != null)
                    props.put(descriptor.getName().toLowerCase(), new PropertyDescriptorWrapper(descriptor, descriptor.getReadMethod(), writer));
            }
            CLASS_PROPERTY_CACHE.put(type.getName(), props);
        }
        return props;
    }

    /**
     * 通过内省机制，获取一个JavaBean的所有属性(属性：一个类的成员变量，并且Getter与Setter至少存在一个)
     *
     * @param type     JavaBean类型
     * @param readable 是否必须可读
     * @param writable 是否必须可写
     * @return 属性描述器数组
     */
    public static Map<String, PropertyDescriptorWrapper> getBeanPropertyDes(Class<?> type, boolean readable, boolean writable) {
        Map<String, PropertyDescriptorWrapper> props = CLASS_PROPERTY_CACHE.get(type.getName() + readable + writable);
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            props = new HashMap<>();
            Method writer;
            Method reader;
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                writer = descriptor.getWriteMethod();
                reader = descriptor.getReadMethod();
                if (!("class".equals(descriptor.getName()) && Class.class == descriptor.getPropertyType()) && (!readable || reader != null) && (!writable || writer != null)) {
                    props.put(descriptor.getName().toLowerCase(), new PropertyDescriptorWrapper(descriptor, reader, writer));
                }
            }
            CLASS_PROPERTY_CACHE.put(type.getName() + readable + writable, props);
        }
        return props;
    }

    /**
     * 通过内省机制获取一个JavaBean的所有属性的getter方法
     *
     * @param c JavaBean类型
     * @return getter方法集合
     */
    public static List<Method> resolveGetters(Class<?> c) {
        Collection<PropertyDescriptorWrapper> ps;
        List<Method> list = new ArrayList<>();
        ps = getBeanPropertyDes(c, true, false).values();
        for (PropertyDescriptorWrapper p : ps) {
            if (p.reader != null) {
                list.add(p.reader);
            }
        }
        return list;
    }

    /**
     * 通过内省机制，获取一个JavaBean的所有属性
     *
     * @param c JavaBean类型
     * @return 属性描述器数组
     */
    private static PropertyDescriptor[] propertyDescriptors(Class<?> c) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(c);
        } catch (IntrospectionException e) {
            throw new SpinException("解析Bean属性异常", e);
        }
        return beanInfo.getPropertyDescriptors();
    }

    /**
     * 属性描述器
     */
    public static class PropertyDescriptorWrapper {
        public PropertyDescriptor descriptor;
        public Class<?> protertyType;
        public Method reader;
        public Method writer;

        public PropertyDescriptorWrapper(PropertyDescriptor descriptor, Method reader, Method writer) {
            this.descriptor = descriptor;
            this.protertyType = descriptor.getPropertyType();
            this.reader = reader;
            this.writer = writer;
        }

        public PropertyDescriptorWrapper(PropertyDescriptor descriptor) {
            this.descriptor = descriptor;
            this.protertyType = descriptor.getPropertyType();
            this.reader = descriptor.getReadMethod();
            this.writer = descriptor.getWriteMethod();
        }

        public PropertyDescriptor getDescriptor() {
            return descriptor;
        }

        public void setDescriptor(PropertyDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        public Class<?> getProtertyType() {
            return protertyType;
        }

        public void setProtertyType(Class<?> protertyType) {
            this.protertyType = protertyType;
        }

        public Method getReader() {
            return reader;
        }

        public void setReader(Method reader) {
            this.reader = reader;
        }

        public Method getWriter() {
            return writer;
        }

        public void setWriter(Method writer) {
            this.writer = writer;
        }
    }

    /**
     * 复制JavaBean的属性到另一个JavaBean中，直接反射字段值，不通过getter/setter
     * <p>如果不指定字段列表，并且源对象是JavaBean，则会拷贝所有字段</p>
     *
     * @param src    源实体
     * @param dest   目标实体
     * @param fields 字段名列表
     */
    public static void copyTo(Object src, Object dest, String... fields) {
        if (null == src || null == dest) {
            return;
        }
        if ((null == fields || fields.length == 0)) {
            if (isJavaBean(src)) {
                Set<String> srcFields = getBeanPropertyDes(src.getClass(), false, false).values().stream().map(p -> p.getDescriptor().getName()).collect(Collectors.toSet());
                fields = getBeanPropertyDes(dest.getClass(), false, false).values().stream().map(p -> p.getDescriptor().getName()).filter(srcFields::contains).toArray(String[]::new);
            } else {
                throw new SpinException("非JavaBean请指定需要Copy的属性列表");
            }
        }

        for (String field : fields) {
            copyPropertie(src, dest, field);
        }
    }

    public static <T, V, P> void copyTo(T src, V dest, Function<T, P> getter, BiConsumer<V, P> setter) {
        if (null == src || null == dest || null == getter || null == setter)
            return;
        setter.accept(dest, getter.apply(src));
    }

    public static <T, V, P1, P2>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }
    }

    public static <T, V, P1, P2, P3>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }
    }


    public static <T, V, P1, P2, P3, P4>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }
    }

    public static <T, V, P1, P2, P3, P4, P5>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4,
                Function<T, P5> getter5, BiConsumer<V, P5> setter5
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }

        if (null != getter5 && null != setter5) {
            setter5.accept(dest, getter5.apply(src));
        }
    }

    public static <T, V, P1, P2, P3, P4, P5, P6>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4,
                Function<T, P5> getter5, BiConsumer<V, P5> setter5,
                Function<T, P6> getter6, BiConsumer<V, P6> setter6
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }

        if (null != getter5 && null != setter5) {
            setter5.accept(dest, getter5.apply(src));
        }

        if (null != getter6 && null != setter6) {
            setter6.accept(dest, getter6.apply(src));
        }
    }

    public static <T, V, P1, P2, P3, P4, P5, P6, P7>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4,
                Function<T, P5> getter5, BiConsumer<V, P5> setter5,
                Function<T, P6> getter6, BiConsumer<V, P6> setter6,
                Function<T, P7> getter7, BiConsumer<V, P7> setter7
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }

        if (null != getter5 && null != setter5) {
            setter5.accept(dest, getter5.apply(src));
        }

        if (null != getter6 && null != setter6) {
            setter6.accept(dest, getter6.apply(src));
        }

        if (null != getter7 && null != setter7) {
            setter7.accept(dest, getter7.apply(src));
        }
    }

    public static <T, V, P1, P2, P3, P4, P5, P6, P7, P8>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4,
                Function<T, P5> getter5, BiConsumer<V, P5> setter5,
                Function<T, P6> getter6, BiConsumer<V, P6> setter6,
                Function<T, P7> getter7, BiConsumer<V, P7> setter7,
                Function<T, P8> getter8, BiConsumer<V, P8> setter8
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }

        if (null != getter5 && null != setter5) {
            setter5.accept(dest, getter5.apply(src));
        }

        if (null != getter6 && null != setter6) {
            setter6.accept(dest, getter6.apply(src));
        }

        if (null != getter7 && null != setter7) {
            setter7.accept(dest, getter7.apply(src));
        }

        if (null != getter8 && null != setter8) {
            setter8.accept(dest, getter8.apply(src));
        }
    }

    public static <T, V, P1, P2, P3, P4, P5, P6, P7, P8, P9>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4,
                Function<T, P5> getter5, BiConsumer<V, P5> setter5,
                Function<T, P6> getter6, BiConsumer<V, P6> setter6,
                Function<T, P7> getter7, BiConsumer<V, P7> setter7,
                Function<T, P8> getter8, BiConsumer<V, P8> setter8,
                Function<T, P9> getter9, BiConsumer<V, P9> setter9
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }

        if (null != getter5 && null != setter5) {
            setter5.accept(dest, getter5.apply(src));
        }

        if (null != getter6 && null != setter6) {
            setter6.accept(dest, getter6.apply(src));
        }

        if (null != getter7 && null != setter7) {
            setter7.accept(dest, getter7.apply(src));
        }

        if (null != getter8 && null != setter8) {
            setter8.accept(dest, getter8.apply(src));
        }

        if (null != getter9 && null != setter9) {
            setter9.accept(dest, getter9.apply(src));
        }
    }

    private static void copyPropertie(Object src, Object dest, String fieldName) {
        Field f1 = ReflectionUtils.findField(src.getClass(), fieldName);
        Field f2 = ReflectionUtils.findField(dest.getClass(), fieldName);
        if (f1 == null) {
            throw new SpinException(fieldName + "不存在于" + src.getClass().getSimpleName());
        }
        if (f2 == null) {
            throw new SpinException(fieldName + "不存在于" + dest.getClass().getSimpleName());
        }
        ReflectionUtils.makeAccessible(f1);
        ReflectionUtils.makeAccessible(f2);
        Object o1 = ReflectionUtils.getField(f1, src);
        ReflectionUtils.setField(f2, dest, o1);
    }
}
