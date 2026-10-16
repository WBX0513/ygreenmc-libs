package net.ygreenmc.math.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface FunctionAttribute {

    String NULL = "\u0000__NULL__\u0000";

    String value() default "";

    String defaultValue() default NULL;
}