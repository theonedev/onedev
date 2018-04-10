package io.onedev.server.web.editable.polymorphic;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.launcher.loader.AppLoader;
import io.onedev.launcher.loader.ImplementationRegistry;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.editable.annotation.Horizontal;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.editable.annotation.Vertical;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class PolymorphicPropertyEditor extends PropertyEditor<Serializable> {

	private static final String BEAN_EDITOR_ID = "beanEditor";
	
	private final List<Class<?>> implementations = new ArrayList<>();

	private final boolean vertical;
	
	private Fragment fragment;
	
	public PolymorphicPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Serializable> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		
		Class<?> baseClass = propertyDescriptor.getPropertyClass();
		ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
		implementations.addAll(registry.getImplementations(baseClass));
		
		Preconditions.checkArgument(
				!implementations.isEmpty(), 
				"Can not find implementations for '" + baseClass + "'.");
		
		EditableUtils.sortAnnotatedElements(implementations);
		
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
		if (propertyGetter.getAnnotation(Vertical.class) != null)
			vertical = true;
		else if (propertyGetter.getAnnotation(Horizontal.class) != null)
			vertical = false;
		else 
			vertical = true;
	}

	private String getDisplayName(Class<?> clazz) {
		String displayName = EditableUtils.getDisplayName(clazz);
		displayName = Application.get().getResourceSettings().getLocalizer().getString(displayName, this, displayName);
		return displayName;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (vertical) {
			fragment = new Fragment("content", "verticalFrag", this);
			fragment.add(AttributeAppender.append("class", " vertical"));
		} else {
			fragment = new Fragment("content", "horizontalFrag", this);
			fragment.add(AttributeAppender.append("class", " horizontal"));
		}
		
		add(fragment);
		
		List<String> implementationNames = new ArrayList<String>();
		for (Class<?> each: implementations)
			implementationNames.add(getDisplayName(each));
				
		WebMarkupContainer typeSelectorContainer = new WebMarkupContainer("typeSelectorContainer");
		typeSelectorContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (hasErrors(false))
					return " has-error";
				else
					return "";
			}
			
		}));
		
		fragment.add(typeSelectorContainer);
		
		DropDownChoice<String> typeSelector = new DropDownChoice<String>("typeSelector", new IModel<String>() {
			
			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				Component beanEditor = fragment.get(BEAN_EDITOR_ID);
				if (beanEditor instanceof BeanEditor) {
					return EditableUtils.getDisplayName(((BeanEditor) beanEditor).getBeanDescriptor().getBeanClass());
				} else {
					return null;
				}
			}

			@Override
			public void setObject(String object) {
				Serializable propertyValue = null;
				for (Class<?> each: implementations) {
					if (getDisplayName(each).equals(object)) {
						try {
							propertyValue = (Serializable) each.newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
						break;
					}
				}
				fragment.replace(newBeanEditor(propertyValue));
			}
			
		}, implementationNames) {

			@Override
			protected String getNullValidDisplayValue() {
				NameOfEmptyValue nameOfEmptyValue = getPropertyDescriptor().getPropertyGetter().getAnnotation(NameOfEmptyValue.class);
				if (nameOfEmptyValue != null)
					return nameOfEmptyValue.value();
				else
					return super.getNullValidDisplayValue();
			}
			
		};
		
		typeSelector.setNullValid(!getPropertyDescriptor().isPropertyRequired());
		
		typeSelector.setLabel(Model.of(getPropertyDescriptor().getDisplayName(this)));
		
		typeSelector.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
				target.add(fragment.get(BEAN_EDITOR_ID));
			}
			
		});
		typeSelectorContainer.add(typeSelector);
		
		fragment.add(newBeanEditor(getModelObject()));
	}
	
	@Override
	protected String getErrorClass() {
		return null;
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof BeanUpdating) {
			event.stop();
			onPropertyUpdating(((BeanUpdating)event.getPayload()).getHandler());
		}		
	}
	
	private Component newBeanEditor(Serializable propertyValue) {
		Component beanEditor;
		if (propertyValue != null) {
			beanEditor = BeanContext.editBean(BEAN_EDITOR_ID, propertyValue);
		} else {
			beanEditor = new WebMarkupContainer(BEAN_EDITOR_ID);
		}
		beanEditor.setOutputMarkupId(true);
		beanEditor.setOutputMarkupPlaceholderTag(true);
		return beanEditor;
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return ((ErrorContext) fragment.get(BEAN_EDITOR_ID)).getErrorContext(pathSegment);
	}

	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		Component beanEditor = fragment.get(BEAN_EDITOR_ID);
		if (beanEditor instanceof BeanEditor) 
			return ((BeanEditor) beanEditor).getConvertedInput();
		else 
			return null;
	}

}
