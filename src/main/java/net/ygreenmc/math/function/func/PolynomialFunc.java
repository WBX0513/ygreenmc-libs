package net.ygreenmc.math.function.func;

import net.ygreenmc.math.function.AbstractFunction;
import net.ygreenmc.math.function.Creator;
import net.ygreenmc.math.function.FunctionAttribute;
import net.ygreenmc.math.function.FunctionPlugin;

import java.util.Arrays;

@FunctionPlugin(name = "polynomial", aliases = {"poly"}, description = "多项式函数")
public final class PolynomialFunc extends AbstractFunction {

    private final double[] coefficients;

    public PolynomialFunc(double... coefficients) {
        super("polynomial", 1, "多项式函数 c0 + c1*x + c2*x^2 + ...");
        if (coefficients == null || coefficients.length == 0) {
            throw new IllegalArgumentException("coefficients must not be empty");
        }
        this.coefficients = coefficients.clone();
    }

    @Creator
    public static PolynomialFunc create(
            @FunctionAttribute(value = "coefficients", defaultValue = "0") String raw) {
        return new PolynomialFunc(parse(raw));
    }

    private static double[] parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new double[]{0.0};
        }
        String[] parts = raw.trim().split("[,\\s]+");
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Double.parseDouble(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid coefficient '" + parts[i] + "' at index " + i, e);
            }
        }
        return result;
    }

    public double[] coefficients() { return coefficients.clone(); }

    public int degree() { return coefficients.length - 1; }

    public PolynomialFunc derivative() {
        if (coefficients.length <= 1) {
            return new PolynomialFunc(0.0);
        }
        double[] d = new double[coefficients.length - 1];
        for (int i = 1; i < coefficients.length; i++) {
            d[i - 1] = coefficients[i] * i;
        }
        return new PolynomialFunc(d);
    }

    @Override
    public double evaluate(double... args) {
        checkArity(args);
        double x = args[0];
        double result = 0.0;
        for (int i = coefficients.length - 1; i >= 0; i--) {
            result = result * x + coefficients[i];
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("polynomial(");
        for (int i = 0; i < coefficients.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(coefficients[i]);
        }
        return sb.append(')').toString();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof PolynomialFunc
                && Arrays.equals(coefficients, ((PolynomialFunc) o).coefficients));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coefficients);
    }
}