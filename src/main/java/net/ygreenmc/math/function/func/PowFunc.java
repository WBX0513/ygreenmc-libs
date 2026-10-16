package net.ygreenmc.math.function.func;

import net.ygreenmc.math.function.AbstractFunction;
import net.ygreenmc.math.function.Creator;
import net.ygreenmc.math.function.Function;
import net.ygreenmc.math.function.FunctionPlugin;

import java.util.Objects;

@FunctionPlugin(name = "pow", aliases = {"power"}, description = "幂函数 base^exponent")
public final class PowFunc extends AbstractFunction {

    private final Function base;
    private final Function exponent;

    public PowFunc(Function base, double exponent) {
        this(base, new ConstFunc(exponent));
    }

    public PowFunc(Function base, Function exponent) {
        super("pow", -1, "base^exponent");
        this.base = Objects.requireNonNull(base, "base");
        this.exponent = Objects.requireNonNull(exponent, "exponent");
    }

    public PowFunc(double exponent) {
        this(new VariableFunc(0), new ConstFunc(exponent));
    }

    public static PowFunc xx() {
        return new PowFunc(new VariableFunc(0), new VariableFunc(0));
    }

    public static PowFunc ofX(Function exponent) {
        return new PowFunc(new VariableFunc(0), exponent);
    }

    @Creator
    public static PowFunc create() {
        return new PowFunc(new VariableFunc(0), new ConstFunc(2));
    }

    public Function base() { return base; }
    public Function exponent() { return exponent; }

    @Override
    public double evaluate(double... args) {
        return Math.pow(base.evaluate(args), exponent.evaluate(args));
    }

    @Override
    public String toString() {
        return "(" + base + ")^(" + exponent + ")";
    }
}