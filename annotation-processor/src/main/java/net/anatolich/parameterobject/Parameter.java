package net.anatolich.parameterobject;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import org.apache.commons.lang3.StringUtils;

public class Parameter {

    private final VariableElement parameter;

    public Parameter(VariableElement parameter) {
        this.parameter = parameter;
    }

    public MethodSpec getter() {
        final String getterName = String.format("get%s", StringUtils.capitalize(parameter.getSimpleName().toString()));
        return MethodSpec.methodBuilder(getterName)
            .returns(TypeName.get(parameter.asType()))
            .addStatement("return $N", parameter.getSimpleName())
            .addModifiers(Modifier.PUBLIC)
            .build();
    }

    public FieldSpec field() {
        return FieldSpec.builder(TypeName.get(parameter.asType()), parameter.getSimpleName().toString())
            .addModifiers(Modifier.PRIVATE)
            .addModifiers(Modifier.FINAL)
            .build();
    }

    public ParameterSpec parameter() {
        return ParameterSpec.builder(
            TypeName.get(parameter.asType()), parameter.getSimpleName().toString())
            .build();
    }

    public CodeBlock addToMapCode(String mapVariableName) {
        final Name parameterName = parameter.getSimpleName();
        return CodeBlock.of("$N.put($S, $N);\n", mapVariableName, parameterName, parameterName);
    }

    public CodeBlock readFromMapCode(String mapVariableName) {
        final Name parameterName = parameter.getSimpleName();
        final TypeName parameterType = TypeName.get(parameter.asType());
        return CodeBlock.of("this.$N = ($T) $N.get($S);\n", parameterName, parameterType, mapVariableName, parameterName);
    }

    public CodeBlock assignParameterToField() {
        final Name parameterName = parameter.getSimpleName();
        return CodeBlock.of("this.$N = $N;\n", parameterName, parameterName);
    }
}
