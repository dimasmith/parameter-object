package net.anatolich.parameterobject;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import org.apache.commons.lang3.StringUtils;

@SupportedAnnotationTypes("net.anatolich.parameterobject.ParameterObject")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ParameterObjectAnnotationProcessor extends AbstractProcessor {

    private Filer filer;
    private Elements elementUtils;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<? extends Element> annotatedMethods = roundEnv.getElementsAnnotatedWith(ParameterObject.class);
        for (Element annotatedMethod : annotatedMethods) {
            final ExecutableElement method = (ExecutableElement) annotatedMethod;
            final TypeElement classOfMethod = (TypeElement) annotatedMethod.getEnclosingElement();
            final Name packageName = elementUtils.getPackageOf(classOfMethod).getQualifiedName();
            final ClassName parametersClass = ClassName.get(
                packageName.toString(),
                classOfMethod.getSimpleName() + StringUtils.capitalize(annotatedMethod.getSimpleName().toString())
                    + "Parameters");
            final Builder parameterObjectClassSpecBuilder = TypeSpec.classBuilder(parametersClass)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL);

            final List<? extends VariableElement> parameters = method.getParameters();

            for (VariableElement parameter : parameters) {
                final FieldSpec field = buildField(parameter);
                final MethodSpec getter = buildGetter(parameter);
                parameterObjectClassSpecBuilder.addField(field);
                parameterObjectClassSpecBuilder.addMethod(getter);
            }

            parameterObjectClassSpecBuilder.addMethod(buildAllArgsConstructor(parameters));
            parameterObjectClassSpecBuilder.addMethod(buildFromMapConstructor(parameters));
            parameterObjectClassSpecBuilder.addMethod(buildFromMapFactoryMethod(parametersClass));
            parameterObjectClassSpecBuilder.addMethod(buildToMapMethod(parameters));

            try {
                JavaFile.builder(packageName.toString(), parameterObjectClassSpecBuilder.build())
                    .build()
                    .writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Kind.ERROR, "Cannot generate parameters class. " + e.getMessage());
            }
        }
        return true;
    }

    private MethodSpec buildGetter(VariableElement parameter) {
        final String getterName = String.format("get%s", StringUtils.capitalize(parameter.getSimpleName().toString()));
        return MethodSpec.methodBuilder(getterName)
            .returns(TypeName.get(parameter.asType()))
            .addStatement("return $N", parameter.getSimpleName())
            .addModifiers(Modifier.PUBLIC)
            .build();
    }

    private FieldSpec buildField(VariableElement parameter) {
        return FieldSpec.builder(TypeName.get(parameter.asType()), parameter.getSimpleName().toString())
            .addModifiers(Modifier.PRIVATE)
            .addModifiers(Modifier.FINAL)
            .build();
    }

    private MethodSpec buildAllArgsConstructor(List<? extends VariableElement> parameters) {
        final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC);
        for (VariableElement parameter : parameters) {
            constructorBuilder
                .addParameter(
                    ParameterSpec.builder(
                        TypeName.get(parameter.asType()), parameter.getSimpleName().toString())
                        .build());

            constructorBuilder
                .addStatement("this.$N = $N", parameter.getSimpleName(), parameter.getSimpleName());
        }
        return constructorBuilder.build();
    }

    private MethodSpec buildFromMapConstructor(List<? extends VariableElement> parameters) {
        final String argumentName = "parameters";
        final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
            .addParameter(ParameterSpec.builder(
                ParameterizedTypeName.get(Map.class, String.class, Object.class),
                argumentName
            ).build())
            .addModifiers(Modifier.PRIVATE);
        for (VariableElement parameter : parameters) {
            constructorBuilder
                .addStatement("this.$N = ($T) $N.get($S)",
                    parameter.getSimpleName(),
                    TypeName.get(parameter.asType()),
                    argumentName,
                    parameter.getSimpleName()
                );
        }
        return constructorBuilder.build();
    }

    private MethodSpec buildFromMapFactoryMethod(ClassName className) {
        final String argumentName = "parameters";
        final MethodSpec.Builder factoryMethodBuilder = MethodSpec.methodBuilder("fromMap")
            .addParameter(ParameterSpec.builder(
                ParameterizedTypeName.get(Map.class, String.class, Object.class),
                argumentName
            ).build())
            .returns(className)
            .addStatement("return new $T($N)", className, argumentName)
            .addModifiers(Modifier.PUBLIC)
            .addModifiers(Modifier.STATIC);
        return factoryMethodBuilder.build();
    }

    private MethodSpec buildToMapMethod(List<? extends VariableElement> parameters) {
        final MethodSpec.Builder mapperMethodBuilder = MethodSpec.methodBuilder("toMap")
            .returns(ParameterizedTypeName.get(Map.class, String.class, Object.class))
            .addModifiers(Modifier.PUBLIC);
        final String resultVariable = "result";
        mapperMethodBuilder.addStatement("final $T $N = new $T()",
            ParameterizedTypeName.get(Map.class, String.class, Object.class),
            resultVariable,
            ParameterizedTypeName.get(HashMap.class, String.class, Object.class)
        );
        for (VariableElement parameter : parameters) {
            mapperMethodBuilder
                .addStatement("$N.put($S, $N)", resultVariable, parameter.getSimpleName(), parameter.getSimpleName());
        }
        mapperMethodBuilder.addStatement("return $N", resultVariable);

        return mapperMethodBuilder.build();
    }
}
