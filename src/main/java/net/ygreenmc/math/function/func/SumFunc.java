package net.ygreenmc.math.function.func;

import net.ygreenmc.math.function.AbstractFunction;
import net.ygreenmc.math.function.Creator;
import net.ygreenmc.math.function.Function;
import net.ygreenmc.math.function.FunctionElement;
import net.ygreenmc.math.function.FunctionPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@FunctionPlugin(name = "sum", aliases = {"add"}, description = "多个函数求和")
public final class SumFunc extends AbstractFunction {

    private final List<Function> terms;

    public SumFunc(Function... terms) {
        super("sum", -1, "多个函数求和");
        Objects.requireNonNull(terms, "terms");
        this.terms = List.copyOf(Arrays.asList(terms));
    }

    @Creator
    public static SumFunc create(@FunctionElement List<Function> terms) {
        if (terms == null || terms.isEmpty()) {
            return new SumFunc(new ConstFunc(0));
        }
        return new SumFunc(terms.toArray(new Function[0]));
    }

    public List<Function> terms() { return terms; }

    @Override
    public double evaluate(double... args) {
        double sum = 0.0;
        for (Function f : terms) {
            sum += f.evaluate(args);
        }
        return sum;
    }

    @Override
    public String toString() {
        if (terms.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < terms.size(); i++) {
            if (i > 0) {
                sb.append(" + ");
            }
            sb.append(terms.get(i));
        }
        return sb.append(')').toString();
    }
}