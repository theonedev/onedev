package io.onedev.server.web.editable.polymorphic;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.ValueEditor;
import io.onedev.server.annotation.ExcludedProperties;

@SuppressWarnings("serial")
public class PolymorphicPropertyEditor extends PropertyEditor<Serializable> {

	private static final String BEAN_EDITOR_ID = "beanEditor";
	
	private final List<Class<?>> implementations = new ArrayList<>();
	
	private final Set<String> excludedProperties = new HashSet<>();

	public PolymorphicPropertyEditor(String id, PropertyDescriptor descriptor, IModel<Serializable> propertyModel) {
		super(id, descriptor, propertyModel);
		
		Class<?> baseClass = descriptor.getPropertyClass();
		ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
		implementations.addAll(registry.getImplementations(baseClass));
		
		Preconditions.checkArgument(
				!implementations.isEmpty(), 
				"Can not find implementations for '" + baseClass + "'.");
		
		EditableUtils.sortAnnotatedElements(implementations);
		
		ExcludedProperties excludedPropertiesAnnotation = 
				descriptor.getPropertyGetter().getAnnotation(ExcludedProperties.class);
		if (excludedPropertiesAnnotation != null) {
			for (String each: excludedPropertiesAnnotation.value())
				excludedProperties.add(each);
		}
	}

	private String getDisplayName(Class<?> clazz) {
		String displayName = EditableUtils.getDisplayName(clazz);
		displayName = Application.get().getResourceSettings().getLocalizer().getString(displayName, this, displayName);
		return displayName;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<String> implementationNames = new ArrayList<String>();
		for (Class<?> each: implementations)
			implementationNames.add(getDisplayName(each));
				
		WebMarkupContainer typeSelectorContainer = new WebMarkupContainer("typeSelectorContainer");
		typeSelectorContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (hasErrorMessage())
					return " is-invalid";
				else
					return "";
			}
			
		}));
		
		add(typeSelectorContainer);
		
		DropDownChoice<String> typeSelector = new DropDownChoice<String>("typeSelector", new IModel<String>() {
			
			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				Component beanEditor = PolymorphicPropertyEditor.this.get(BEAN_EDITOR_ID);
				if (beanEditor instanceof BeanEditor) {
					return EditableUtils.getDisplayName(((BeanEditor) beanEditor).getDescriptor().getBeanClass());
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
							propertyValue = (Serializable) each.getDeclaredConstructor().newInstance();
							Serializable prevPropertyValue = PolymorphicPropertyEditor.this.getConvertedInput();
							if (prevPropertyValue != null) {
								BeanDescriptor prevDescriptor = new BeanDescriptor(prevPropertyValue.getClass());
								for (List<PropertyDescriptor> prevGroupProperties: prevDescriptor.getProperties().values()) {
									for (PropertyDescriptor prevProperty: prevGroupProperties) {
										Class<?> declaringClass = prevProperty.getPropertyGetter().getDeclaringClass();
										Class<?> baseClass = descriptor.getPropertyClass();
										if (!prevProperty.isPropertyExcluded() && declaringClass.isAssignableFrom(baseClass)) 
											prevProperty.copyProperty(prevPropertyValue, propertyValue);
									}
								}
							}
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
								| InvocationTargetException | NoSuchMethodException | SecurityException e) {
							throw new RuntimeException(e);
						}
						break;
					}
				}
				PolymorphicPropertyEditor.this.replace(newBeanEditor(propertyValue));
			}
			
		}, implementationNames) {

			@Override
			protected String getNullValidDisplayValue() {
				ComponentContext.push(new ComponentContext(PolymorphicPropertyEditor.this));
				try {
					String placeholder = EditableUtils.getPlaceholder(getDescriptor().getPropertyGetter());
					if (placeholder != null)
						return placeholder;
					else
						return super.getNullValidDisplayValue();
				} finally {
					ComponentContext.pop();
				}
			}
			
		};
		
		typeSelector.setNullValid(!getDescriptor().isPropertyRequired());
		
		typeSelector.setLabel(Model.of(getDescriptor().getDisplayName()));
		
		typeSelector.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
				target.add(typeSelectorContainer.get("typeDescription"));
				
				Component beanEditor = get(BEAN_EDITOR_ID);
				target.add(beanEditor);
				
				if (beanEditor instanceof BeanEditor) {
					target.appendJavaScript(String.format("$('#%s').addClass('property-defined');", 
							PolymorphicPropertyEditor.this.getMarkupId()));
				} else {
					target.appendJavaScript(String.format("$('#%s').removeClass('property-defined');", 
							PolymorphicPropertyEditor.this.getMarkupId()));
				}
			}
			
		});
		typeSelectorContainer.add(typeSelector);
		
		typeSelectorContainer.add(new Label("typeDescription", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Component beanEditor = PolymorphicPropertyEditor.this.get(BEAN_EDITOR_ID);				
				if (beanEditor instanceof BeanEditor) {
					Class<?> beanClass = ((BeanEditor) beanEditor).getDescriptor().getBeanClass(); 
					return EditableUtils.getDescription(beanClass);
				} else {
					return null;
				}
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				Component beanEditor = PolymorphicPropertyEditor.this.get(BEAN_EDITOR_ID);				
				if (beanEditor instanceof BeanEditor) {
					Class<?> beanClass = ((BeanEditor) beanEditor).getDescriptor().getBeanClass(); 
					setVisible(EditableUtils.getDescription(beanClass) != null);
				} else {
					setVisible(false);
				}
			}
			
		}.setOutputMarkupPlaceholderTag(true).setEscapeModelStrings(false));
		
		add(newBeanEditor(getModelObject()));
		
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (get(BEAN_EDITOR_ID) instanceof BeanEditor)
					return "property-polymorphic property-defined";
				else
					return "property-polymorphic";					
			}
			
		}));
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
			beanEditor = BeanContext.edit(BEAN_EDITOR_ID, propertyValue, excludedProperties, true);
		} else {
			beanEditor = new WebMarkupContainer(BEAN_EDITOR_ID);
		}
		beanEditor.setOutputMarkupId(true);
		beanEditor.setOutputMarkupPlaceholderTag(true);
		return beanEditor;
	}

	@Override
	public void error(PathNode propertyNode, Path pathInProperty, String errorMessage) {
		Component editor = get(BEAN_EDITOR_ID);
		if (editor instanceof ValueEditor)
			((ValueEditor<?>) editor).error(propertyNode, pathInProperty, errorMessage);
		else
			super.error(propertyNode, pathInProperty, errorMessage);
	}

	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		Component beanEditor = get(BEAN_EDITOR_ID);
		if (beanEditor instanceof BeanEditor) 
			return ((BeanEditor) beanEditor).getConvertedInput();
		else 
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
