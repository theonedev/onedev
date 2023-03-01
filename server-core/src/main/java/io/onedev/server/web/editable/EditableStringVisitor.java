package io.onedev.server.web.editable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import io.onedev.server.util.BeanUtils;
import io.onedev.server.annotation.Editable;

public class EditableStringVisitor {
	
	private final Consumer<String> consumer;
	
	public EditableStringVisitor(Consumer<String> consumer) {
		this.consumer = consumer;
	}
	
	@SuppressWarnings("unchecked")
	public void visitProperties(Object object, Class<? extends Annotation> visitableMarker) {
		for (Method getter: BeanUtils.findGetters(object.getClass())) {
			if (getter.getAnnotation(Editable.class) != null && BeanUtils.findSetter(getter) != null) {
				try {
					Object propertyValue = getter.invoke(object);
					if (propertyValue != null) {
						Class<?> propertyClass = propertyValue.getClass();
						if (getter.getAnnotation(visitableMarker) != null) {
							try {
								if (propertyValue instanceof String) {
									consumer.accept((String) propertyValue);
								} else if (propertyValue instanceof List) {
									for (Object element: (List<String>) propertyValue) { 
										if (element instanceof String) {
											consumer.accept((String) element);
										} else if (element instanceof List) {
											for (String element2: (List<String>) element) {  
												if (element2 != null)
													consumer.accept((String) element2);
											}
										} else {
											throw new RuntimeException("Unexpected list element type: " + element.getClass());
										}
									}
								}
							} catch (Exception e) {
								String message = String.format("Error visiting (class: %s, property: %s)", 
										propertyClass, BeanUtils.getPropertyName(getter));
								throw new RuntimeException(message, e);
							}
						} else if (propertyClass.getAnnotation(Editable.class) != null) {
							visitProperties(propertyValue, visitableMarker);
						} else if (propertyValue instanceof List) {
							for (Object element: (List<?>)propertyValue) { 
								if (element != null && element.getClass().getAnnotation(Editable.class) != null)
									visitProperties(element, visitableMarker);
							}
						}
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}	
		
}
