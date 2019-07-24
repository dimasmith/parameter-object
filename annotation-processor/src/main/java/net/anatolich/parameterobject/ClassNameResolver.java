package net.anatolich.parameterobject;

import com.squareup.javapoet.ClassName;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import org.apache.commons.lang3.StringUtils;

public class ClassNameResolver {

    private final Elements elements;

    public ClassNameResolver(Elements elements) {
        this.elements = elements;
    }

    public ClassName resolve(ExecutableElement method, ParameterObject annotation) {
        return ClassName.get(
            packageName(method, annotation),
            className(method, annotation));
    }

    private String className(ExecutableElement method, ParameterObject annotation) {
        if (StringUtils.isNotBlank(annotation.className())) {
            return annotation.className();
        } else {
            return defaultClassName(method);
        }
    }

    private String defaultClassName(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        String className = method.getEnclosingElement().getSimpleName().toString();
        return String.format("%s%sParameters", className, StringUtils.capitalize(methodName));
    }

    private String packageName(ExecutableElement method, ParameterObject annotation) {
        if (StringUtils.isNotBlank(annotation.packageName())) {
            return annotation.packageName();
        } else {
            return defaultPackageName(method);
        }
    }

    private String defaultPackageName(ExecutableElement method) {
        return elements.getPackageOf(method.getEnclosingElement()).getQualifiedName().toString();
    }
}
