package xyz.quartzframework.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.core.annotation.AnnotatedElementUtils.findAllMergedAnnotations;
import static org.springframework.util.ClassUtils.getUserClass;

@UtilityClass
public class AopAnnotationUtils {

    private static final Map<Class<?>, Map<Method, List<Annotation>>> annotationCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Annotation> List<T> getApplicableAnnotations(Method method, Class<T> annotation) {
        val methodCache = annotationCache.computeIfAbsent(annotation, a -> new ConcurrentHashMap<>());
        val annotations = methodCache.computeIfAbsent(method, m -> {
            val declaringClass = getUserClass(method.getDeclaringClass());
            val methodAnnotations = findAllMergedAnnotations(method, annotation);
            val classAnnotations = findAllMergedAnnotations(declaringClass, annotation);
            return Collections.unmodifiableList(Stream.concat(classAnnotations.stream(), methodAnnotations.stream()).collect(Collectors.toList()));
        });
        return (List<T>) annotations;
    }

    public <T extends Annotation> Set<T> getApplicableAnnotations(Class<?> clazz, Class<T> annotation) {
        return findAllMergedAnnotations(clazz, annotation);
    }
}