package net.ygreenmc.math.function.func;

import net.ygreenmc.math.function.AbstractFunction;
import net.ygreenmc.math.function.Creator;
import net.ygreenmc.math.function.FunctionPlugin;

@FunctionPlugin(name = "var", aliases = {"x"}, description = "变量 args[index]")
public final class VariableFunc extends AbstractFunction {

    private final int index;

    public VariableFunc() {
        this(0);
    }

    public VariableFunc(int index) {
        super("var", -1, "变量 args[" + index + "]");
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        this.index = index;
    }

    @Creator
    public static VariableFunc create() {
        return new VariableFunc(0);
    }

    public int index() { return index; }

    @Override
    public double evaluate(double... args) {
        if (index >= args.length) {
            throw new IllegalArgumentException(
                    "Variable 'args[" + index + "]' requires at least "
                            + (index + 1) + " argument(s)");
        }
        return args[index];
    }

    @Override
    public String toString() {
        return index == 0 ? "x" : "args[" + index + "]";
    }
}