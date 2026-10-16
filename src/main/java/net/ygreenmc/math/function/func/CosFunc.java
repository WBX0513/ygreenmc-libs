package net.ygreenmc.math.function.func;

import net.ygreenmc.math.function.AbstractFunction;
import net.ygreenmc.math.function.Creator;
import net.ygreenmc.math.function.Function;
import net.ygreenmc.math.function.FunctionElement;
import net.ygreenmc.math.function.FunctionPlugin;

@FunctionPlugin(name = "cos", aliases = {"cosine"}, description = "余弦函数")
public final class CosFunc extends AbstractFunction {

    private final Function inner;

    public CosFunc() {
        this(null);
    }

    public CosFunc(Function inner) {
        super("cos", -1, "cos");
        this.inner = inner;
    }

    @Creator
    public static CosFunc create(@FunctionElement Function inner) {
        return new CosFunc(inner);
    }

    public Function inner() { return inner; }

    @Override
    public double evaluate(double... args) {
        double v;
        if (inner != null) {
            v = inner.evaluate(args);
        } else {
            if (args.length < 1) {
                throw new IllegalArgumentException("cos(x) requires at least 1 argument");
            }
            v = args[0];
        }
        return Math.cos(v);
    }

    @Override
    public String toString() {
        return inner == null ? "cos(x)" : "cos(" + inner + ")";
    }
}