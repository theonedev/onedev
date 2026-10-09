package io.onedev.server.web.editable;

import static io.onedev.server.web.translation.Translation._T;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.wicket.Session;
import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.WorkingPeriod;
import io.onedev.server.model.Project;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.interpolative.VariableInterpolator;

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

	public static boolean isTranslatable(AnnotatedElement element) {
		var editable = element.getAnnotation(Editable.class);
		return editable == null || editable.translatable();
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
	 * 			defined description, or <tt>null</tt> if description cannot be found
	 */
	@Nullable
	public static String getDescription(AnnotatedElement element) {
		Editable editable = element.getAnnotation(Editable.class);
		if (editable == null) {
			return null;
		} else if (element.getAnnotation(Interpolative.class) != null) {
			return appendDescription(getDescription(element, editable), VariableInterpolator.getHelp());
		} else if (element.getAnnotation(WorkingPeriod.class) != null) {
			var timeTrackingSetting = OneDev.getInstance(SettingService.class).getIssueSetting().getTimeTrackingSetting();
			return appendDescription(getDescription(element, editable), timeTrackingSetting.getWorkingPeriodHelp());
		} else {
			String description = getDescription(element, editable);
			if (description.length() != 0) 
				return description;
			else 
				return null;
		} 
	}
	
	private static String getDescription(AnnotatedElement element, Editable editable) {
		String description = getBaseDescription(element, editable);
		if (editable.topPlaceholder().length() != 0 || editable.topPlaceholderProvider().length() != 0) {
			Project project = Project.get();
			String inheritanceDescription = null;
			if (project != null && !Project.DEFAULT_ID.equals(project.getId())) {
				if (project.getParent() != null) {
					inheritanceDescription = _T("Leave empty to inherit from the parent project.");
				} else if (OneDev.getInstance(SubscriptionService.class).isSubscriptionActive()) {
					inheritanceDescription = _T("Leave empty to use the <a href='https://docs.onedev.io/administration-guide/project-defaults' target='_blank'>default setting</a>.");
				}
			}
			if (inheritanceDescription != null)
				description = appendDescription(description, inheritanceDescription);
		}
		return description;
	}

	private static String appendDescription(String description, String help) {
		description = description.stripTrailing();
		if (description.isEmpty())
			return help;
		if (description.matches("(?is).*<br\\s*/?>$"))
			return description + help;
		// Inspect rendered text so punctuation inside markup or HTML entities is recognized too.
		var text = HtmlUtils.getText(description).stripTrailing();
		if (!text.matches("(?s).*[.!?。！？…][\\p{Pe}\\p{Pf}\"']*$")) {
			var language = Session.exists() ? Session.get().getLocale().getLanguage() : "en";
			description += language.equals("zh") || language.equals("ja") ? "。" : ".";
		}
		return description + " " + help;
	}

	private static String getBaseDescription(AnnotatedElement element, Editable editable) {
		String description = editable.description();
		if (description.length() != 0) {
			if (isTranslatable(element))
				return _T(description);
			else
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

	public static boolean isDisplayPlaceholderAsValue(AnnotatedElement element) {
		return element.getAnnotation(Editable.class).displayPlaceholderAsValue();
	}
	
	@Nullable
	public static String getPlaceholder(AnnotatedElement element) {
		Editable editable = element.getAnnotation(Editable.class);
		Project project = Project.get();
		if (project != null && project.getParent() == null) {
			if ((editable.topPlaceholder().length() != 0 || editable.topPlaceholderProvider().length() != 0)
					&& !Project.DEFAULT_ID.equals(project.getId()) && OneDev.getInstance(SubscriptionService.class).isSubscriptionActive()) {
				return _T("Use default setting");
			}
			String placeholder = editable.topPlaceholder();
			if (placeholder.length() != 0) {
				if (isTranslatable(element))
					return _T(placeholder);
				else
					return placeholder;
			} else if (editable.topPlaceholderProvider().length() != 0) {
				Class<?> clazz;
				if (element instanceof Class) 
					clazz = (Class<?>) element;
				else if (element instanceof Method)
					clazz = ((Method) element).getDeclaringClass();
				else 
					throw new RuntimeException("Unexpected element type: " + element);
				return (String) ReflectionUtils.invokeStaticMethod(clazz, editable.topPlaceholderProvider());
			}
		}
		String placeholder = editable.placeholder();
		if (placeholder.length() != 0) {
			if (isTranslatable(element))
				return _T(placeholder);
			else
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
		annotatedElements.sort((element1, element2) -> {
			var order1 = getOrder(element1);
			var order2 = getOrder(element2);
			if (order1 != order2)
				return order1 - order2;
			else 
				return getDisplayName(element1).toLowerCase().compareTo(getDisplayName(element2).toLowerCase());
		});
	}

	public static String getGroupedType(Class<?> clazz) {
		Editable editable = clazz.getAnnotation(Editable.class);
		if (editable != null && editable.group().length() != 0)
			return _T(editable.group()) + " / " + _T(getDisplayName(clazz));
		else
			return _T(getDisplayName(clazz));
	}
	
}
