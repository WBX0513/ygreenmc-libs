package net.ygreenmc.math.function.func;

import net.ygreenmc.math.function.AbstractFunction;
import net.ygreenmc.math.function.Creator;
import net.ygreenmc.math.function.FunctionAttribute;
import net.ygreenmc.math.function.FunctionPlugin;

@FunctionPlugin(name = "clamp", description = "把输入限制在 [min, max] 区间")
public final class ClampFunc extends AbstractFunction {

    private final double min;
    private final double max;

    public ClampFunc(double min, double max) {
        super("clamp", 1, "把输入限制在 [min, max] 区间");
        if (min > max) {
            throw new IllegalArgumentException("min(" + min + ") > max(" + max + ")");
        }
        this.min = min;
        this.max = max;
    }

    @Creator
    public static ClampFunc create(
            @FunctionAttribute(value = "min", defaultValue = "0") double min,
            @FunctionAttribute(value = "max", defaultValue = "1") double max) {
        return new ClampFunc(min, max);
    }

    public double min() { return min; }
    public double max() { return max; }

    @Override
    public double evaluate(double... args) {
        checkArity(args);
        return Math.max(min, Math.min(max, args[0]));
    }
}
