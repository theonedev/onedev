package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.GeneralException;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.WordUtils;

public class EditableUtils {
	
	/**
	 * Get display name of specified element from description parameter of {@link Editable} annotation. 
	 * If the annotation is not defined or name parameter is not available in the annotation, the 
	 * element name itself will be transferred to non-camel case and returned.
	 *
	 * @param element
	 * 			annotated element to get name from
	 * @return
	 * 			display name of the element
	 */
	public static String getName(AnnotatedElement element) {
		Editable editable = element.getAnnotation(Editable.class);
		if (editable != null && editable.name().trim().length() != 0)
			return editable.name();
		else if (element instanceof Class)
			return WordUtils.uncamel(((Class<?>)element).getSimpleName());
		else if (element instanceof Field) 
			return WordUtils.uncamel(WordUtils.capitalize(((Field)element).getName()));
		else if (element instanceof Method)
			return StringUtils.substringAfter(WordUtils.uncamel(((Method)element).getName()), " ");
		else if (element instanceof Package) 
			return ((Package)element).getName();
		else
			throw new GeneralException("Invalid element type: " + element.getClass().getName());
	}
	
	/**
	 * Get description of specified element from description parameter of {@link Editable} annotation
	 *
	 * @param element
	 * 			annotated element to get description from
	 * @return
	 * 			defined description, or <tt>null</tt> if description can not be found
	 */
	public static String getDescription(AnnotatedElement element) {
		Editable editable = element.getAnnotation(Editable.class);
		if (editable != null && editable.description().trim().length() != 0) {
			return editable.description().trim();
		} else {
			return null;
		}
	}

	/**
	 * Get display order parameter defined in {@link Editable} annotation of specified element.
	 * 
	 * @param element
	 * 			annotated element to get order from
	 * @return
	 * 			defined order, or {@link Integer.MAX_VALUE} if Editable annotation is not found
	 */
	public static int getOrder(AnnotatedElement element) {
		Editable editable = element.getAnnotation(Editable.class);
		if (editable != null)
			return editable.order();
		else
			return Integer.MAX_VALUE;
	}
	
	/**
	 * Sort specified elements by order parameter defined in {@link Editable} annotation.
	 * 
	 * @param annotatedElements
	 *			annotated elements to be sorted 
	 */
	public static <T extends AnnotatedElement> void sortAnnotatedElements(List<T> annotatedElements) {
		Collections.sort(annotatedElements, new Comparator<T>(){

			public int compare(T element1, T element2) {
				return getOrder(element1) - getOrder(element2);
			}
			
		});
	}
	
	public static Class<?> getElementClass(Type listType) {
		if (listType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType)listType;
			Type rawType = parameterizedType.getRawType();
			if (rawType instanceof Class<?>) {
				Class<?> rawClazz = (Class<?>) rawType;
				if (List.class.isAssignableFrom(rawClazz)) {
					Type elementType = parameterizedType.getActualTypeArguments()[0];
					if (elementType instanceof Class<?>) { 
						return (Class<?>) elementType;
					}
				}
			}
		}
		return null;
	}
	
	public static void validate(Serializable bean) {
		EditContext context = AppLoader.getInstance(EditSupportRegistry.class).getBeanEditContext(bean);
		context.validate();
		List<ValidationError> errors = context.getValidationErrors(true);
		if (!errors.isEmpty()) {
			StringBuffer buffer = new StringBuffer();
			
			for (ValidationError error: errors) {
				buffer.append(error.toString()).append("\n");
			}
			
			throw new ValidationException(buffer.toString());
		}
	}

}
