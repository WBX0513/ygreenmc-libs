package net.ygreenmc.math.function;

import net.ygreenmc.math.function.func.ClampFunc;
import net.ygreenmc.math.function.func.ConstFunc;
import net.ygreenmc.math.function.func.CosFunc;
import net.ygreenmc.math.function.func.LinearFunc;
import net.ygreenmc.math.function.func.LogFunc;
import net.ygreenmc.math.function.func.PolynomialFunc;
import net.ygreenmc.math.function.func.PowFunc;
import net.ygreenmc.math.function.func.SinFunc;
import net.ygreenmc.math.function.func.SumFunc;
import net.ygreenmc.math.function.func.VariableFunc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FunctionManager {

    static {
        // ---- 内置函数自动注册 ----
        registerBuiltins();
    }

    private FunctionManager() {
    }

    private static void registerBuiltins() {
        FunctionRegistry.register(VariableFunc.class);
        FunctionRegistry.register(ConstFunc.class);
        FunctionRegistry.register(LinearFunc.class);
        FunctionRegistry.register(PowFunc.class);
        FunctionRegistry.register(SinFunc.class);
        FunctionRegistry.register(CosFunc.class);
        FunctionRegistry.register(SumFunc.class);
        FunctionRegistry.register(LogFunc.class);
        FunctionRegistry.register(ClampFunc.class);
        FunctionRegistry.register(PolynomialFunc.class);
    }

    public static void register(Class<?> pluginClass) {
        FunctionRegistry.register(pluginClass);
    }

    public static void scan(String... packages) {
        FunctionRegistry.scan(packages);
    }

    public static Function create(String name) {
        return create(name, Collections.emptyMap(), Collections.emptyList());
    }

    public static Function create(String name, Map<String, String> attributes) {
        return create(name, attributes, Collections.emptyList());
    }

    public static Function create(String name,
                                  Map<String, String> attributes,
                                  List<Function> children) {
        FunctionRegistry.Registration reg = FunctionRegistry.lookup(name);
        if (reg == null) {
            throw new FunctionCreationException("Unknown function: '" + name
                    + "'. Available: " + FunctionRegistry.names());
        }

        Map<String, String> attrs = attributes == null
                ? Collections.emptyMap() : attributes;
        List<Function> kids = children == null
                ? Collections.emptyList() : children;

        Method creator = reg.creator();
        Object[] args = resolveArguments(creator, attrs, kids);

        try {
            return (Function) creator.invoke(null, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new FunctionCreationException(
                    "Creator of function '" + name + "' threw an exception: " + cause, cause);
        } catch (ReflectiveOperationException e) {
            throw new FunctionCreationException(
                    "Failed to create function '" + name + "'", e);
        }
    }

    public static java.util.Set<String> names() {
        return FunctionRegistry.names();
    }

    public static boolean contains(String name) {
        return FunctionRegistry.contains(name);
    }

    private static Object[] resolveArguments(Method creator,
                                             Map<String, String> attrs,
                                             List<Function> children) {
        Parameter[] params = creator.getParameters();
        Object[] result = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            result[i] = resolveArgument(creator, params[i], attrs, children);
        }
        return result;
    }

    private static Object resolveArgument(Method creator,
                                          Parameter p,
                                          Map<String, String> attrs,
                                          List<Function> children) {
        Class<?> type = p.getType();

        FunctionAttribute attrAnn = p.getAnnotation(FunctionAttribute.class);
        if (attrAnn != null) {
            String key = attrAnn.value().isEmpty() ? p.getName() : attrAnn.value();
            String raw = attrs.get(key);
            if (raw == null) {
                String def = attrAnn.defaultValue();
                if (FunctionAttribute.NULL.equals(def)) {
                    throw new FunctionCreationException("Missing required attribute '" + key
                            + "' for " + creator.getDeclaringClass().getSimpleName());
                }
                raw = def;
            }
            return convert(raw, type, key, creator);
        }

        if (p.isAnnotationPresent(FunctionElement.class)) {
            if (Function.class.isAssignableFrom(type)) {
                return children.isEmpty() ? null : children.get(0);
            }
            if (Collection.class.isAssignableFrom(type)) {
                return Collections.unmodifiableList(new ArrayList<>(children));
            }
            throw new FunctionCreationException(
                    "@FunctionElement parameter must be a Function or Collection<Function>: " + p);
        }

        if (Function.class.isAssignableFrom(type)) {
            return children.isEmpty() ? null : children.get(0);
        }
        if (Collection.class.isAssignableFrom(type)) {
            return Collections.unmodifiableList(new ArrayList<>(children));
        }
        if (Map.class.isAssignableFrom(type)) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(attrs));
        }
        if (type == double[].class) {
            double[] values = new double[children.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = children.get(i).evaluate();
            }
            return values;
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convert(String raw, Class<?> type, String key, Method creator) {
        String v = raw == null ? "" : raw.trim();
        try {
            if (type == String.class || type == CharSequence.class || type == Object.class) {
                return raw;
            }
            if (type == double.class || type == Double.class)   return Double.parseDouble(v);
            if (type == float.class || type == Float.class)     return Float.parseFloat(v);
            if (type == int.class || type == Integer.class)     return Integer.parseInt(v);
            if (type == long.class || type == Long.class)       return Long.parseLong(v);
            if (type == short.class || type == Short.class)     return Short.parseShort(v);
            if (type == byte.class || type == Byte.class)       return Byte.parseByte(v);
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(v);
            if (type == char.class || type == Character.class) {
                if (v.isEmpty()) throw new NumberFormatException("empty char");
                return v.charAt(0);
            }
            if (type == BigDecimal.class) return new BigDecimal(v);
            if (type == BigInteger.class) return new BigInteger(v);
            if (type.isEnum()) return Enum.valueOf((Class<? extends Enum>) type, v);
        } catch (RuntimeException e) {
            throw badValue(raw, type, key, creator);
        }
        throw new FunctionCreationException("Unsupported attribute type " + type.getName()
                + " for parameter '" + key + "' in " + creator.getDeclaringClass().getName());
    }

    private static FunctionCreationException badValue(String raw, Class<?> type,
                                                      String key, Method creator) {
        return new FunctionCreationException("Cannot convert value '" + raw + "' to "
                + type.getSimpleName() + " for attribute '" + key + "' of "
                + creator.getDeclaringClass().getSimpleName());
    }
}