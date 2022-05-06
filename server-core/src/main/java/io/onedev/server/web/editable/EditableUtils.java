package io.onedev.server.web.editable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.WorkingPeriod;

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
		else 
			return BeanUtils.getDisplayName(element);
	}
	
	/**
	 * Get group of specified element from group parameter of {@link Editable} annotation.
	 *
	 * @param element
	 * 			annotated element to get group from
	 * @return
	 * 			group name of specified element, or <tt>null</tt> if not defined
	 */
	@Nullable
	public static String getGroup(AnnotatedElement element) {
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
	@Nullable
	public static String getIcon(AnnotatedElement element) {
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
	@Nullable
	public static String getDescription(AnnotatedElement element) {
		Editable editable = element.getAnnotation(Editable.class);
		if (editable == null) {
			return null;
		} else if (element.getAnnotation(Interpolative.class) != null) {
			String description = getDescription(element, editable);
			if (description.length() != 0) {
				if (description.endsWith("<br>")) {
					description += VariableInterpolator.HELP;
				} else {
					if (!description.endsWith("."))
						description += ".";
					description += " " + VariableInterpolator.HELP;
				}
			} else {
				description = VariableInterpolator.HELP;
			}
			return StringUtils.replace(description, "$docRoot", OneDev.getInstance().getDocRoot());
		} else if (element.getAnnotation(WorkingPeriod.class) != null) {
			String description = getDescription(element, editable);
			if (description.length() != 0) {
				if (!description.endsWith("."))
					description += ".";
				description += " " + DateUtils.WORKING_PERIOD_HELP;
			} else {
				description = DateUtils.WORKING_PERIOD_HELP;
			}
			return StringUtils.replace(description, "$docRoot", OneDev.getInstance().getDocRoot());
		} else {
			String description = getDescription(element, editable);
			if (description.length() != 0) 
				return StringUtils.replace(description, "$docRoot", OneDev.getInstance().getDocRoot());
			else 
				return null;
		} 
	}
	
	private static String getDescription(AnnotatedElement element, Editable editable) {
		String description = editable.description();
		if (description.length() != 0) {
			return description;
		} else if (editable.descriptionProvider().length() != 0) {
			Class<?> clazz;
			if (element instanceof Class) 
				clazz = (Class<?>) element;
			else if (element instanceof Method)
				clazz = ((Method) element).getDeclaringClass();
			else 
				throw new RuntimeException("Unexpected element type: " + element);
			description = (String) ReflectionUtils.invokeStaticMethod(clazz, editable.descriptionProvider());
			if (description == null)
				description = "";
			return description;
		} else {
			return "";
		}
	}

	@Nullable
	public static String getPlaceholder(AnnotatedElement element) {
		Editable editable = element.getAnnotation(Editable.class);
		String placeholder = editable.placeholder();
		if (placeholder.length() != 0) {
			return placeholder;
		} else if (editable.placeholderProvider().length() != 0) {
			Class<?> clazz;
			if (element instanceof Class) 
				clazz = (Class<?>) element;
			else if (element instanceof Method)
				clazz = ((Method) element).getDeclaringClass();
			else 
				throw new RuntimeException("Unexpected element type: " + element);
			return (String) ReflectionUtils.invokeStaticMethod(clazz, editable.placeholderProvider());
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

}
