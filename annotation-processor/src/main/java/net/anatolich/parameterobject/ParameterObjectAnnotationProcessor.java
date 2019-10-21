package net.anatolich.parameterobject;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("net.anatolich.parameterobject.ParameterObject")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ParameterObjectAnnotationProcessor extends AbstractProcessor {

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Filer filer = processingEnv.getFiler();
        final Messager messager = processingEnv.getMessager();
        final Elements elementUtils = processingEnv.getElementUtils();
        final ClassNameResolver classNameResolver = new ClassNameResolver(elementUtils);

        final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(ParameterObject.class);
        for (Element annotatedElement : annotatedElements) {
            if (annotatedElement.getKind() != ElementKind.METHOD) {
                messager.printMessage(Kind.ERROR,
                    "ParameterObject anotation only allowed on methods",
                    annotatedElement);
            }

            final ExecutableElement method = (ExecutableElement) annotatedElement;
            final ClassName parametersClassName = classNameResolver
                .resolve(method, method.getAnnotation(ParameterObject.class));

            final ParameterObjectClassBuilder parameterObjectClassBuilder =
                new ParameterObjectClassBuilder(parametersClassName, method);
            final TypeSpec parameterObjectClass = parameterObjectClassBuilder.build();

            try {
                JavaFile.builder(parametersClassName.packageName(), parameterObjectClass)
                    .build()
                    .writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Kind.ERROR, "Cannot generate parameters class. " + e.getMessage());
            }
        }
        return true;
    }
}
