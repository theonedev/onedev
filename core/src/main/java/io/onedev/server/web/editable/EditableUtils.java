package io.onedev.server.web.editable;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.Validator;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.utils.BeanUtils;
import io.onedev.commons.utils.ClassUtils;
import io.onedev.commons.utils.ReflectionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.web.editable.annotation.Editable;

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
	public static String getDisplayName(AnnotatedElement element) {
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
			throw new RuntimeException("Invalid element type: " + element.getClass().getName());
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
		if (editable != null && editable.group().trim().length() != 0)
			return editable.group();
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
		annotatedElements.sort((element1, element2) -> getOrder(element1) - getOrder(element2));
	}
	
	public static boolean hasEditableProperties(Class<?> beanClass) {
	    for (Method getter: BeanUtils.findGetters(ClassUtils.unproxy(beanClass))) {
	        Method setter = BeanUtils.findSetter(getter);
	        if (setter != null && getter.getAnnotation(Editable.class) != null)
	        	return true;
	    }
	    return false;
	}
	
	public static boolean isDefaultInstanceValid(Class<? extends Serializable> beanClass) {
		Serializable bean = ReflectionUtils.instantiateClass(beanClass);
		return AppLoader.getInstance(Validator.class).validate(bean).isEmpty();
	}

}
