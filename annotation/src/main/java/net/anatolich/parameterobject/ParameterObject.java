package net.anatolich.parameterobject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ParameterObject {

    /**
     * Desired package name. If not specified the parameter object will be in the same package
     * as annotated class.
     */
    String packageName() default "";

    /**
     * Desired class name. If not specified then default class name generation strategy will be applied.
     */
    String className() default "";
}
