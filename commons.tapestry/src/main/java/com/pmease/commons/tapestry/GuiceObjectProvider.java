package com.pmease.commons.tapestry;

import java.lang.annotation.Annotation;
import java.util.List;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

public class GuiceObjectProvider implements ObjectProvider {

	private Injector injector;

	public GuiceObjectProvider(Injector injector) {
		this.injector = injector;
	}

	@SuppressWarnings("unchecked")
	public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator) {
		TypeLiteral<?> type = TypeLiteral.get(objectType);
		final List<?> bindings = injector.findBindingsByType(type);
		if (bindings.size() == 1)
			return injector.getInstance(objectType);

		for (int i = 0; i < bindings.size(); ++i) {
			Binding<?> binding = (Binding<?>) bindings.get(i);
			Class<? extends Annotation> annotationType = binding.getKey().getAnnotationType();
			if (annotationType != null) {
				Annotation annotation = annotationProvider.getAnnotation(annotationType);
				if (annotation == null && annotationType == com.google.inject.name.Named.class)
					annotation = annotationProvider.getAnnotation(javax.inject.Named.class);
				if (annotation != null) {
					if (annotation instanceof com.google.inject.name.Named) {
						com.google.inject.name.Named providedNamed = (com.google.inject.name.Named) annotation;
						com.google.inject.name.Named bindingNamed = (com.google.inject.name.Named) binding.getKey().getAnnotation();
						if (providedNamed.value().equals(bindingNamed.value()))
							return (T) injector.getInstance(binding.getKey());
					} else if (annotation instanceof javax.inject.Named) {
						javax.inject.Named providedNamed = (javax.inject.Named) annotation;
						com.google.inject.name.Named bindingNamed = (com.google.inject.name.Named) binding.getKey().getAnnotation();
						if (providedNamed.value().equals(bindingNamed.value()))
							return (T) injector.getInstance(binding.getKey());
					} else {
						return (T) injector.getInstance(binding.getKey());						
					}
				}
			} else {
				return (T) injector.getInstance(binding.getKey());
			}
		}
		
		return null;
	}

}
