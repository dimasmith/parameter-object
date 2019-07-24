package net.anatolich.parameterobject;

import com.squareup.javapoet.ClassName;
import java.lang.annotation.Annotation;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ClassNameResolverTest {

    private ClassNameResolver classNameResolver;
    private Elements elements;

    @BeforeEach
    void createResolver() {
        elements = Mockito.mock(Elements.class);
        classNameResolver = new ClassNameResolver(elements);
    }

    @Test
    void resolveDefaultName() {
        String methodName = "method";
        String className = "Class";
        String packageName = "net.anatolich.util";
        ClassName resolvedName = classNameResolver.resolve(methodElement(methodName, className, packageName),
            annotation(null, null));

        Assertions.assertThat(resolvedName.packageName())
            .isEqualTo(packageName);
        Assertions.assertThat(resolvedName.simpleName())
            .isEqualTo("ClassMethodParameters");
    }

    @Test
    void resolveClassNameWithCustomPackage() {
        String methodName = "method";
        String className = "Class";
        String packageName = "net.anatolich.util";
        String customPackageName = "custom.util";

        ClassName resolvedName = classNameResolver.resolve(
            methodElement(methodName, className, packageName),
            annotation(customPackageName, null)
        );

        Assertions.assertThat(resolvedName.packageName())
            .as("annotation overrides default package")
            .isEqualTo(customPackageName);
        Assertions.assertThat(resolvedName.simpleName())
            .isEqualTo("ClassMethodParameters");
    }

    @Test
    void resolveClassNameWithCustomClassName() {
        String methodName = "method";
        String className = "Class";
        String packageName = "net.anatolich.util";
        String customClassName = "AwesomeParameters";

        ClassName resolvedName = classNameResolver.resolve(
            methodElement(methodName, className, packageName),
            annotation(null, customClassName)
        );

        Assertions.assertThat(resolvedName.packageName())
            .as("annotation overrides default package")
            .isEqualTo(packageName);
        Assertions.assertThat(resolvedName.simpleName())
            .isEqualTo(customClassName);
    }

    private ExecutableElement methodElement(String methodName, String className, String packageName) {
        final ExecutableElement methodElementMock = Mockito.mock(ExecutableElement.class);
        final TypeElement classElementMock = Mockito.mock(TypeElement.class);
        final PackageElement packageElementMock = Mockito.mock(PackageElement.class);
        Mockito.when(methodElementMock.getSimpleName()).thenReturn(new SimpleName(methodName));
        Mockito.when(methodElementMock.getEnclosingElement()).thenReturn(classElementMock);
        Mockito.when(classElementMock.getSimpleName()).thenReturn(new SimpleName(className));
        Mockito.when(elements.getPackageOf(Mockito.any())).thenReturn(packageElementMock);
        Mockito.when(packageElementMock.getQualifiedName()).thenReturn(new SimpleName(packageName));
        return methodElementMock;
    }

    private static class SimpleName implements Name {

        private final String value;

        private SimpleName(String value) {
            this.value = value;
        }

        @Override
        public boolean contentEquals(CharSequence cs) {
            return value.contentEquals(cs);
        }

        @Override
        public int length() {
            return value.length();
        }

        @Override
        public char charAt(int index) {
            return value.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return value.subSequence(start, end);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private ParameterObject annotation(String packageName, String className) {
        return new ParameterObject() {
            @Override
            public String packageName() {
                return (packageName == null) ? "" : packageName;
            }

            @Override
            public String className() {
                return (className == null) ? "" : className;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ParameterObject.class;
            }
        };
    }
}
