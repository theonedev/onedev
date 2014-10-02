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

import javax.annotation.Nullable;
import javax.validation.Validator;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.GeneralException;
import com.pmease.commons.util.JavassistUtils;
import com.pmease.commons.util.ReflectionUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.WordUtils;

public class EditableUtils {
	
	/**
	 * Get display name of specified element from name parameter of {@link Editable} annotation. 
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
	 * Get category of specified element from category parameter of {@link Editable} annotation.
	 *
	 * @param element
	 * 			annotated element to get category from
	 * @return
	 * 			category name of specified element, or <tt>null</tt> if not defined
	 */
	public static @Nullable String getCategory(AnnotatedElement element) {
		Editable editable = element.getAnnotation(Editable.class);
		if (editable != null && editable.category().trim().length() != 0)
			return editable.category();
		else
			return null;
	}

	/**
	 * Get icon of specified element from icon parameter of {@link Editable} annotation.
	 *
	 * @param element
	 * 			annotated element to get icon from
	 * @return
	 * 			icon name of specified element, or <tt>null</tt> if not defined
	 */
	public static @Nullable String getIcon(AnnotatedElement element) {
		Editable editable = element.getAnnotation(Editable.class);
		if (editable != null && editable.icon().trim().length() != 0)
			return editable.icon();
		else
			return null;
	}

	/**
	 * Get description of specified element from description parameter of {@link Editable} annotation
	 *
	 * @param element
	 * 			annotated element to get description from
	 * @return
	 * 			defined description, or <tt>null</tt> if description can not be found
	 */
	public static @Nullable String getDescription(AnnotatedElement element) {
		Editable editable = element.getAnnotation(Editable.class);
		if (editable != null) {
			if (editable.description().length() != 0)
				return editable.description();
			if (editable.descriptionProvider().length() != 0) {
				Class<?> clazz;
				if (element instanceof Method)
					clazz = ((Method) element).getDeclaringClass();
				else if (element instanceof Class) 
					clazz = (Class<?>) element;
				else 
					clazz = ((Field) element).getDeclaringClass();
				return (String) ReflectionUtils.invokeStaticMethod(clazz, editable.descriptionProvider());
			}
		}
		return null;
	}

	/**
	 * Get display order parameter defined in {@link Editable} annotation of specified element.
	 * 
	 * @param element
	 * 			annotated element to get order from
	 * @return
	 * 			defined order, or {@link Integer#MAX_VALUE} if Editable annotation is not found
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
	
	public static boolean hasEditableProperties(Class<?> beanClass) {
	    for (Method getter: BeanUtils.findGetters(JavassistUtils.unproxy(beanClass))) {
	        Method setter = BeanUtils.findSetter(getter);
	        if (setter != null && getter.getAnnotation(Editable.class) != null) {
	        	return true;
	        }
	    }
	    return false;
	}
	
	public static boolean isDefaultInstanceValid(Class<? extends Serializable> beanClass) {
		Serializable bean = ReflectionUtils.instantiateClass(beanClass);
		return AppLoader.getInstance(Validator.class).validate(bean).isEmpty();
	}

}
