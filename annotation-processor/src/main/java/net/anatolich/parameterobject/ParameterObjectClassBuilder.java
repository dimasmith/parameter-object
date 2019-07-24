package net.anatolich.parameterobject;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

public class ParameterObjectClassBuilder {

    private final Parameters parameters;
    private final ClassName parametersClassName;

    public ParameterObjectClassBuilder(ClassName parametersClassName, ExecutableElement method) {
        this.parametersClassName = parametersClassName;
        this.parameters = new Parameters(method.getParameters());
    }

    public TypeSpec build() {
        return TypeSpec.classBuilder(this.parametersClassName)
            .addModifiers(Modifier.PUBLIC)
            .addModifiers(Modifier.FINAL)
            .addFields(buildClassFields())
            .addMethod(buildAllArgsConstructor())
            .addMethod(buildFromMapConstructor())
            .addMethod(buildFromMapFactoryMethod())
            .addMethod(buildToMapMethod())
            .addMethods(buildGetters())
            .build();
    }

    private Iterable<MethodSpec> buildGetters() {
        return parameters.getters();
    }

    private Iterable<FieldSpec> buildClassFields() {
        return parameters.classFields();
    }

    private MethodSpec buildAllArgsConstructor() {
        return MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameters(parameters.parameters())
            .addCode(parameters.assignParameterToField())
            .build();
    }

    private MethodSpec buildFromMapConstructor() {
        final String argumentName = "parameters";
        return MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(ArgumentsMap.parameter(argumentName))
            .addCode(parameters.readFromMapCode(argumentName))
            .build();
    }

    private MethodSpec buildFromMapFactoryMethod() {
        final String argumentName = "parameters";
        return MethodSpec.methodBuilder("fromMap")
            .addModifiers(Modifier.PUBLIC)
            .addModifiers(Modifier.STATIC)
            .returns(parametersClassName)
            .addParameter(ArgumentsMap.parameter(argumentName))
            .addStatement("return new $T($N)", parametersClassName, argumentName)
            .build();
    }

    private MethodSpec buildToMapMethod() {
        final String resultVariable = "result";
        return MethodSpec.methodBuilder("toMap")
            .addModifiers(Modifier.PUBLIC)
            .returns(ArgumentsMap.type())
            .addStatement(ArgumentsMap.initialize(resultVariable))
            .addCode(parameters.addToMapCode(resultVariable))
            .addStatement(ArgumentsMap.returnResult(resultVariable))
            .build();
    }
}
