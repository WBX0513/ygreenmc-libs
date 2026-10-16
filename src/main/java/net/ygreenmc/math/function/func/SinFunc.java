package net.ygreenmc.math.function.func;

import net.ygreenmc.math.function.AbstractFunction;
import net.ygreenmc.math.function.Creator;
import net.ygreenmc.math.function.Function;
import net.ygreenmc.math.function.FunctionElement;
import net.ygreenmc.math.function.FunctionPlugin;

@FunctionPlugin(name = "sin", aliases = {"sine"}, description = "正弦函数")
public final class SinFunc extends AbstractFunction {

    private final Function inner;

    public SinFunc() {
        this(null);
    }

    public SinFunc(Function inner) {
        super("sin", -1, "sin");
        this.inner = inner;
    }

    @Creator
    public static SinFunc create(@FunctionElement Function inner) {
        return new SinFunc(inner);
    }

    public Function inner() { return inner; }

    @Override
    public double evaluate(double... args) {
        double v;
        if (inner != null) {
            v = inner.evaluate(args);
        } else {
            if (args.length < 1) {
                throw new IllegalArgumentException("sin(x) requires at least 1 argument");
            }
            v = args[0];
        }
        return Math.sin(v);
    }

    @Override
    public String toString() {
        return inner == null ? "sin(x)" : "sin(" + inner + ")";
    }
}