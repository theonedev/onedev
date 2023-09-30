package io.onedev.server.web.editable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import io.onedev.server.util.BeanEditContext;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.annotation.Editable;
import io.onedev.server.util.EditContext;

public class EditableStringTransformer {
	
	private final Function<String, String> transformer;
	
	public EditableStringTransformer(Function<String, String> transformer) {
		this.transformer = transformer;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T transformProperties(T object, Class<? extends Annotation> transformativeMarker) {
		EditContext.push(new BeanEditContext(object));
		try {
			Class<T> clazz = (Class<T>) object.getClass();
			var beanDescriptor = new BeanDescriptor(clazz);

			T transformed;
			try {
				transformed = clazz.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}

			for (Field field : BeanUtils.findFields(clazz)) {
				field.setAccessible(true);
				try {
					field.set(transformed, field.get(object));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

			for (Method getter : BeanUtils.findGetters(clazz)) {
				if (getter.getAnnotation(Editable.class) != null) {
					Method setter = BeanUtils.findSetter(getter);
					if (setter != null && beanDescriptor.isPropertyVisible(BeanUtils.getPropertyName(getter))) {
						try {
							Object propertyValue = getter.invoke(transformed);
							if (propertyValue != null) {
								Class<?> propertyClass = propertyValue.getClass();
								if (getter.getAnnotation(transformativeMarker) != null) {
									try {
										if (propertyValue instanceof String) {
											setter.invoke(transformed, transformer.apply((String) propertyValue));
										} else if (propertyValue instanceof List) {
											List<Object> transformedList = new ArrayList<>();
											for (Object element : (List<String>) propertyValue) {
												if (element == null) {
													transformedList.add(element);
												} else if (element instanceof String) {
													transformedList.add(transformer.apply((String) element));
												} else if (element instanceof List) {
													List<String> transformedList2 = new ArrayList<>();
													for (String element2 : (List<String>) element) {
														if (element2 != null)
															transformedList2.add(transformer.apply((String) element2));
														else
															transformedList2.add(element2);
													}
													transformedList.add(transformedList2);
												} else {
													throw new RuntimeException("Unexpected list element type: " + element.getClass());
												}
											}
											setter.invoke(transformed, transformedList);
										}
									} catch (Exception e) {
										String message = String.format("Error transforming (class: %s, property: %s)",
												propertyClass, BeanUtils.getPropertyName(getter));
										throw new RuntimeException(message, e);
									}
								} else if (propertyClass.getAnnotation(Editable.class) != null) {
									setter.invoke(transformed, transformProperties(propertyValue, transformativeMarker));
								} else if (propertyValue instanceof List) {
									List<Object> transformedList = new ArrayList<>();
									for (Object element : (List<?>) propertyValue) {
										if (element != null && element.getClass().getAnnotation(Editable.class) != null)
											transformedList.add(transformProperties(element, transformativeMarker));
										else
											transformedList.add(element);
									}
									setter.invoke(transformed, transformedList);
								}
							}
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}

			return transformed;
		} finally {
			EditContext.pop();
		}
	}	
		
}
