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
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("net.anatolich.parameterobject.ParameterObject")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ParameterObjectAnnotationProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private ClassNameResolver classNameResolver;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        classNameResolver = new ClassNameResolver(processingEnv.getElementUtils());
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<? extends Element> annotatedMethods = roundEnv.getElementsAnnotatedWith(ParameterObject.class);
        for (Element annotatedMethod : annotatedMethods) {
            final ExecutableElement method = (ExecutableElement) annotatedMethod;
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
