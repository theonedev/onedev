package io.onedev.server.web.editable;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.util.JsoupUtils;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.editable.annotation.ShowCondition;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.utils.BeanUtils;
import io.onedev.utils.ReflectionUtils;

public class PropertyDescriptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Class<?> beanClass;
	
	private final String propertyName;
	
	private boolean excluded;
	
	private final Set<String> dependencyPropertyNames = new HashSet<>();
	
	private transient Method propertyGetter;
	
	private transient Method propertySetter;
	
	public PropertyDescriptor(Class<?> beanClass, String propertyName) {
		this.beanClass = beanClass;
		this.propertyName = propertyName;
	}
	
	public PropertyDescriptor(Method propertyGetter) {
		this.beanClass = propertyGetter.getDeclaringClass();
		this.propertyName = BeanUtils.getPropertyName(propertyGetter);
	}
	
	public PropertyDescriptor(PropertyDescriptor propertyDescriptor) {
		this.beanClass = propertyDescriptor.getBeanClass();
		this.propertyName = propertyDescriptor.getPropertyName();
		this.excluded = propertyDescriptor.isExcluded();
	}
	
	public Class<?> getBeanClass() {
		return beanClass;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Method getPropertyGetter() {
		if (propertyGetter == null)
			propertyGetter = BeanUtils.getGetter(beanClass, propertyName);
		return propertyGetter;
	}
	
	public Method getPropertySetter() {
		if (propertySetter == null) 
			propertySetter = BeanUtils.getSetter(getPropertyGetter());
		return propertySetter;
	}

	public boolean isExcluded() {
		return excluded;
	}

	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}

	public void copyProperty(Object fromBean, Object toBean) {
		setPropertyValue(toBean, getPropertyValue(fromBean));
	}

	public Object getPropertyValue(Object bean) {
		try {
			return getPropertyGetter().invoke(bean);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public void setPropertyValue(Object bean, Object propertyValue) {
		try {
			getPropertySetter().invoke(bean, propertyValue);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public Class<?> getPropertyClass() {
		return getPropertyGetter().getReturnType();
	}

	public boolean isPropertyRequired() {
		return getPropertyGetter().getReturnType().isPrimitive()
				|| getPropertyGetter().getAnnotation(NotNull.class) != null 
				|| getPropertyGetter().getAnnotation(NotEmpty.class) != null
				|| getPropertyGetter().getAnnotation(Size.class) != null && getPropertyGetter().getAnnotation(Size.class).min()>=1;
	}

	public boolean isPropertyVisible(PropertyContextAware propertyContextAware) {
		return isPropertyVisible(propertyContextAware, new HashSet<>());
	}
	
	public boolean isPropertyVisible(PropertyContextAware propertyContextAware, Set<String> checkedPropertyNames) {
		if (checkedPropertyNames.contains(getPropertyName()))
			return true;
		checkedPropertyNames.add(getPropertyName());
		
		OneContext.push(new ComponentContext((Component) propertyContextAware));
		try {
			ShowCondition showCondition = getPropertyGetter().getAnnotation(ShowCondition.class);
			if (showCondition != null && !(boolean)ReflectionUtils.invokeStaticMethod(getBeanClass(), showCondition.value()))
				return false;
			for (String propertyName: getDependencyPropertyNames()) {
				PropertyContext<?> propertyContext = propertyContextAware.getPropertyContext(propertyName);
				if (!propertyContext.isPropertyVisible(propertyContextAware, checkedPropertyNames))
					return false;
			}
			return true;
		} finally {
			OneContext.pop();
		}
	}
	
	public Set<String> getDependencyPropertyNames() {
		return dependencyPropertyNames;
	}
	
	public String getDisplayName() {
		return EditableUtils.getDisplayName(getPropertyGetter());
	}
	
	public String getDisplayName(Component component) {
		String displayName = getDisplayName();
		return Application.get().getResourceSettings().getLocalizer().getString(displayName, component, displayName);
	}
	
	public String getDescription() {
		return EditableUtils.getDescription(getPropertyGetter());
	}
	
	public String getDescription(Component component) {
		String description = getDescription();
		if (description != null) {
			OneContext.push(new ComponentContext(component));
			try {
				description = Application.get().getResourceSettings().getLocalizer().getString(description, component, description);
				description = GroovyUtils.evalGString(description);
				return JsoupUtils.clean(description).body().html();
			} finally {
				OneContext.pop();
			}
		} else {
			return null;
		}
	}
}
