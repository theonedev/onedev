package io.onedev.server.web.page.help;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

public abstract class JsonMember implements AnnotatedElement {
	
	private final String name;
	
	private final AnnotatedElement delegate;

	public JsonMember(String name, AnnotatedElement delegate) {
		this.name = name;
		this.delegate = delegate;
	}

	public String getName() {
		return name;
	}
	
	public abstract Class<?> getType();
	
	public abstract Type getGenericType();
	
	public abstract Object getValue(Object obj);
	
	public abstract void setValue(Object obj, Object value);

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return delegate.isAnnotationPresent(annotationClass);
	}

	@Override
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return delegate.getAnnotationsByType(annotationClass);
	}

	@Override
	public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
		return delegate.getDeclaredAnnotation(annotationClass);
	}

	@Override
	public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
		return delegate.getDeclaredAnnotationsByType(annotationClass);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return delegate.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return delegate.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return delegate.getDeclaredAnnotations();
	}
	
}
