package net.ygreenmc.math.function.func;

import net.ygreenmc.math.function.AbstractFunction;
import net.ygreenmc.math.function.Creator;
import net.ygreenmc.math.function.FunctionAttribute;
import net.ygreenmc.math.function.FunctionPlugin;

@FunctionPlugin(name = "const", aliases = {"constant"}, description = "常量函数")
public final class ConstFunc extends AbstractFunction {

    private final double value;

    public ConstFunc(double value) {
        super("const", -1, "常量函数");
        this.value = value;
    }

    @Creator
    public static ConstFunc create(
            @FunctionAttribute(value = "value", defaultValue = "0") double value) {
        return new ConstFunc(value);
    }

    public double value() { return value; }

    @Override
    public double evaluate(double... args) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}