package net.ygreenmc.math.function;

import net.ygreenmc.math.function.func.SumFunc;

import java.util.Objects;

public final class FunctionFactory {

    private FunctionFactory() {
    }

    public static Function getFunction(Function... functions) {
        Objects.requireNonNull(functions, "functions");
        if (functions.length == 0) {
            throw new IllegalArgumentException("At least one function is required");
        }
        if (functions.length == 1) {
            return Objects.requireNonNull(functions[0], "function");
        }
        return new SumFunc(functions);
    }
}