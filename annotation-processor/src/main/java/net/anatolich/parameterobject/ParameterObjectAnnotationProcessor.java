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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
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
            final TypeElement classOfMethod = (TypeElement) annotatedMethod.getEnclosingElement();
            final Name packageName = elementUtils.getPackageOf(classOfMethod).getSimpleName();
            final ClassName parametersClass = ClassName.get(
                packageName.toString(),
                StringUtils.capitalize(annotatedMethod.getSimpleName().toString()) + "Parameters");
            final TypeSpec parameterClassSpec = TypeSpec.classBuilder(parametersClass)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .build();
            try {
                JavaFile.builder(packageName.toString(), parameterClassSpec)
                    .build()
                    .writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Kind.ERROR, "Cannot generate parameters class. " + e.getMessage());
            }
        }
        return true;
    }
}
