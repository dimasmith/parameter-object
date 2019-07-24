package net.anatolich.parameterobject;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.util.HashMap;
import java.util.Map;

public class ArgumentsMap {

    private static final ParameterizedTypeName TYPE = ParameterizedTypeName
        .get(Map.class, String.class, Object.class);
    private static final ParameterizedTypeName IMPLEMENTATION_TYPE = ParameterizedTypeName
        .get(HashMap.class, String.class, Object.class);

    static ParameterSpec parameter(String name) {
        return ParameterSpec.builder(TYPE, name).build();
    }

    static CodeBlock initialize(String variableName) {
        return CodeBlock.of("final $T $N = new $T()", TYPE, variableName, IMPLEMENTATION_TYPE);
    }

    static CodeBlock returnResult(String variableName) {
        return CodeBlock.of("return $N", variableName);
    }

    static ParameterizedTypeName type() {
        return TYPE;
    }
}
