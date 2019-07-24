package net.anatolich.parameterobject;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.lang.model.element.VariableElement;

public class Parameters {

    private final List<Parameter> parameters;

    public Parameters(List<? extends VariableElement> parameters) {
        this.parameters = parameters.stream()
            .map(Parameter::new)
            .collect(Collectors.toList());
    }

    public Iterable<FieldSpec> classFields() {
        return this.specs(Parameter::field);
    }

    public Iterable<MethodSpec> getters() {
        return this.specs(Parameter::getter);
    }

    public Iterable<ParameterSpec> parameters() {
        return this.specs(Parameter::parameter);
    }

    public CodeBlock addToMapCode(String mapVariableName) {
        final Iterable<CodeBlock> codeBlocks = specs(p -> p.addToMapCode(mapVariableName));
        return CodeBlock.join(codeBlocks, "");
    }

    public CodeBlock readFromMapCode(String mapVariableName) {
        final Iterable<CodeBlock> codeBlocks = specs(p -> p.readFromMapCode(mapVariableName));
        return CodeBlock.join(codeBlocks, "");
    }

    public CodeBlock assignParameterToField() {
        final Iterable<CodeBlock> codeBlocks = specs(Parameter::assignParameterToField);
        return CodeBlock.join(codeBlocks, "");
    }

    private <T> Iterable<T> specs(Function<Parameter, T> converter) {
        return parameters.stream()
            .map(converter)
            .collect(Collectors.toList());
    }
}
