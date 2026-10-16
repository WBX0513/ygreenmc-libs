package net.ygreenmc.math.function;

import java.util.Objects;

@FunctionalInterface
public interface Function {

    double evaluate(double... args);

    default int arity() {
        return -1;
    }

    default String name() {
        return getClass().getSimpleName();
    }

    default Function andThen(Function next) {
        Objects.requireNonNull(next, "next");
        Function self = this;
        return new Function() {
            @Override public double evaluate(double... args) { return next.evaluate(self.evaluate(args)); }
            @Override public int arity() { return self.arity(); }
            @Override public String name() { return self.name() + " -> " + next.name(); }
            @Override public String toString() { return name(); }
        };
    }

    static Function of(Function f) {
        return Objects.requireNonNull(f, "f");
    }

    static Function constant(double value) {
        return args -> value;
    }
}
