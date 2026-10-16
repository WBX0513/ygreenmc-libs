package net.ygreenmc.math.function;

import java.util.Objects;

public abstract class AbstractFunction implements Function {

    private final String name;
    private final int arity;
    private final String description;

    protected AbstractFunction(String name, int arity, String description) {
        this.name = Objects.requireNonNull(name, "name");
        this.arity = arity;
        this.description = description == null ? "" : description;
    }

    protected AbstractFunction(String name, int arity) {
        this(name, arity, "");
    }

    @Override public final String name() { return name; }
    @Override public final int arity() { return arity; }
    public final String description() { return description; }

    protected final void checkArity(double... args) {
        if (arity >= 0 && args.length != arity) {
            throw new IllegalArgumentException(
                    "Function '" + name + "' expects " + arity
                            + " argument(s) but got " + args.length);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}