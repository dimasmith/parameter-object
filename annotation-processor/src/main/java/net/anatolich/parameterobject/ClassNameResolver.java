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

    public ClassName resolve(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        String className = method.getEnclosingElement().getSimpleName().toString();
        String packageName = elements.getPackageOf(method.getEnclosingElement()).getQualifiedName().toString();
        String poClassName = String.format("%s%sParameters", className, StringUtils.capitalize(methodName));
        return ClassName.get(packageName, poClassName);
    }
}
