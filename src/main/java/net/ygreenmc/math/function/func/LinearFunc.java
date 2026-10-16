package net.ygreenmc.math.function.func;

import net.ygreenmc.math.function.AbstractFunction;
import net.ygreenmc.math.function.Creator;
import net.ygreenmc.math.function.FunctionAttribute;
import net.ygreenmc.math.function.FunctionPlugin;

@FunctionPlugin(name = "linear", aliases = {"line"}, description = "一次函数 y = kx + b")
public final class LinearFunc extends AbstractFunction {

    private final double k;
    private final double b;

    public LinearFunc(double k, double b) {
        super("linear", 1, "y = kx + b");
        this.k = k;
        this.b = b;
    }

    @Creator
    public static LinearFunc create(
            @FunctionAttribute(value = "k", defaultValue = "1") double k,
            @FunctionAttribute(value = "b", defaultValue = "0") double b) {
        return new LinearFunc(k, b);
    }

    public double k() { return k; }
    public double b() { return b; }

    @Override
    public double evaluate(double... args) {
        checkArity(args);
        return k * args[0] + b;
    }

    @Override
    public String toString() {
        return "(" + k + "*x + " + b + ")";
    }
}