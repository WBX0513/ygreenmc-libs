package net.ygreenmc.math.function;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class FunctionRegistry {

    public static final class Registration {
        private final String name;
        private final Set<String> aliases;
        private final String description;
        private final Class<? extends Function> type;
        private final Method creator;

        Registration(String name, Set<String> aliases, String description,
                     Class<? extends Function> type, Method creator) {
            this.name = name;
            this.aliases = Collections.unmodifiableSet(aliases);
            this.description = description == null ? "" : description;
            this.type = type;
            this.creator = creator;
        }

        public String name() { return name; }
        public Set<String> aliases() { return aliases; }
        public String description() { return description; }
        public Class<? extends Function> type() { return type; }
        public Method creator() { return creator; }

        @Override
        public String toString() {
            return "Function[" + name + " -> " + type.getName() + "]";
        }
    }

    private static final Map<String, Registration> REGISTRY = new ConcurrentHashMap<>();
    private static final Set<Class<?>> PROCESSED = ConcurrentHashMap.newKeySet();
    private static final Set<String> SCANNED_PACKAGES = ConcurrentHashMap.newKeySet();

    private FunctionRegistry() {
    }

    public static synchronized void register(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        FunctionPlugin plugin = clazz.getAnnotation(FunctionPlugin.class);
        if (plugin == null) {
            throw new IllegalArgumentException("@FunctionPlugin missing on " + clazz.getName());
        }
        if (!Function.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                    clazz.getName() + " must implement " + Function.class.getName());
        }
        if (!PROCESSED.add(clazz)) {
            return;
        }

        Method creator = findCreatorMethod(clazz);
        if (creator == null) {
            throw new IllegalArgumentException(
                    "No @Creator static method found in " + clazz.getName());
        }

        @SuppressWarnings("unchecked")
        Class<? extends Function> type = (Class<? extends Function>) clazz;
        Registration reg = new Registration(plugin.name(),
                new LinkedHashSet<>(Arrays.asList(plugin.aliases())),
                plugin.description(), type, creator);

        putUnique(plugin.name(), reg);
        for (String alias : plugin.aliases()) {
            if (alias != null && !alias.isEmpty()) {
                putUnique(alias, reg);
            }
        }
    }

    private static void putUnique(String key, Registration reg) {
        Registration prev = REGISTRY.putIfAbsent(key, reg);
        if (prev != null && prev.type() != reg.type()) {
            throw new IllegalStateException("Duplicate function name/alias: '" + key
                    + "' (" + prev.type().getName() + " vs " + reg.type().getName() + ")");
        }
    }

    private static Method findCreatorMethod(Class<?> clazz) {
        Method fallback = null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Creator.class)) {
                continue;
            }
            if (!Modifier.isStatic(m.getModifiers())) {
                throw new IllegalArgumentException(
                        "@Creator method must be static: " + m);
            }
            if (!Function.class.isAssignableFrom(m.getReturnType())) {
                throw new IllegalArgumentException(
                        "@Creator method must return a Function: " + m);
            }
            try {
                if (!m.canAccess(null)) {
                    m.setAccessible(true);
                }
            } catch (RuntimeException ignored) {
                // 交给后续 invoke 报错
            }
            if (m.getParameterCount() == 0) {
                return m;
            }
            if (fallback == null) {
                fallback = m;
            }
        }
        return fallback;
    }

    public static Registration lookup(String name) {
        return name == null ? null : REGISTRY.get(name);
    }

    public static boolean contains(String name) {
        return name != null && REGISTRY.containsKey(name);
    }

    public static Set<String> names() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    public static Collection<Registration> registrations() {
        return Collections.unmodifiableCollection(new LinkedHashSet<>(REGISTRY.values()));
    }

    public static void clear() {
        REGISTRY.clear();
        PROCESSED.clear();
        SCANNED_PACKAGES.clear();
    }

    public static void scan(String... packages) {
        if (packages == null) {
            return;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = FunctionRegistry.class.getClassLoader();
        }
        for (String pkg : packages) {
            if (pkg == null || pkg.isEmpty() || !SCANNED_PACKAGES.add(pkg)) {
                continue;
            }
            try {
                doScan(pkg, loader);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to scan package: " + pkg, e);
            }
        }
    }

    private static void doScan(String pkg, ClassLoader loader) throws Exception {
        String path = pkg.replace('.', '/');
        Enumeration<URL> urls = loader.getResources(path);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String protocol = url.getProtocol();
            if ("file".equals(protocol)) {
                File dir = new File(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
                scanDirectory(pkg, dir, loader);
            } else if ("jar".equals(protocol)) {
                scanJar(pkg, url, loader);
            }
        }
    }

    private static void scanDirectory(String pkg, File dir, ClassLoader loader) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(pkg + "." + f.getName(), f, loader);
            } else if (f.getName().endsWith(".class")) {
                String simple = f.getName().substring(0, f.getName().length() - 6);
                tryLoad(pkg + "." + simple, loader);
            }
        }
    }

    private static void scanJar(String pkg, URL url, ClassLoader loader) throws IOException {
        JarURLConnection conn = (JarURLConnection) url.openConnection();
        conn.setUseCaches(false);
        try (JarFile jar = conn.getJarFile()) {
            String prefix = pkg.replace('.', '/') + "/";
            List<String> classNames = new ArrayList<>();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                String n = e.getName();
                if (e.isDirectory() || !n.startsWith(prefix) || !n.endsWith(".class")) {
                    continue;
                }
                if (n.endsWith("module-info.class") || n.endsWith("package-info.class")) {
                    continue;
                }
                classNames.add(n.substring(0, n.length() - 6).replace('/', '.'));
            }
            for (String cn : classNames) {
                tryLoad(cn, loader);
            }
        }
    }

    private static void tryLoad(String className, ClassLoader loader) {
        try {
            Class<?> c = Class.forName(className, false, loader);
            if (c.isAnnotationPresent(FunctionPlugin.class)) {
                register(c);
            }
        } catch (Throwable ignored) {
        }
    }
}