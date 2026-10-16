package net.ygreenmc.math.function.func;

import net.ygreenmc.math.function.AbstractFunction;
import net.ygreenmc.math.function.Creator;
import net.ygreenmc.math.function.FunctionAttribute;
import net.ygreenmc.math.function.FunctionPlugin;

@FunctionPlugin(name = "log", aliases = {"logarithm"}, description = "对数函数")
public final class LogFunc extends AbstractFunction {

    private final double base;

    public LogFunc(double base) {
        super("log", 1, "以 base 为底的对数");
        if (base <= 0 || base == 1) {
            throw new IllegalArgumentException("base must be > 0 and != 1, but was " + base);
        }
        this.base = base;
    }

    @Creator
    public static LogFunc create(
            @FunctionAttribute(value = "base", defaultValue = "2.718281828459045") double base) {
        return new LogFunc(base);
    }

    public double base() { return base; }

    @Override
    public double evaluate(double... args) {
        checkArity(args);
        return Math.log(args[0]) / Math.log(base);
    }
}