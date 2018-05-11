package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;

import io.onedev.launcher.loader.AppLoader;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.DefaultValueProvider;
import io.onedev.server.util.editable.annotation.Horizontal;
import io.onedev.server.util.editable.annotation.OmitName;
import io.onedev.server.util.editable.annotation.Vertical;
import io.onedev.server.web.editable.PathSegment.Property;
import io.onedev.server.web.editable.bean.BeanPropertyEditor;
import io.onedev.server.web.editable.polymorphic.PolymorphicPropertyEditor;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.utils.ReflectionUtils;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public class BeanEditor extends ValueEditor<Serializable> {

	public static final String SCRIPT_CONTEXT_BEAN = "beanEditor";
	
	private final BeanDescriptor beanDescriptor;
	
	private final List<PropertyContext<Serializable>> propertyContexts = new ArrayList<>();
	
	private final boolean vertical;
	
	private RepeatingView propertiesView;
	
	public BeanEditor(String id, BeanDescriptor beanDescriptor, IModel<Serializable> model) {
		super(id, model);
		
		this.beanDescriptor = beanDescriptor;
		
		for (PropertyDescriptor propertyDescriptor: beanDescriptor.getPropertyDescriptors())
			propertyContexts.add(PropertyContext.of(propertyDescriptor));
		
		Class<?> beanClass = beanDescriptor.getBeanClass();
		if (beanClass.getAnnotation(Vertical.class) != null)
			vertical = true;
		else if (beanClass.getAnnotation(Horizontal.class) != null)
			vertical = false;
		else 
			vertical = true;
	}

	private boolean hasTransitiveDependency(String dependentPropertyName, String dependencyPropertyName, 
			Set<String> checkedPropertyNames) {
		if (checkedPropertyNames.contains(dependentPropertyName))
			return false;
		checkedPropertyNames.add(dependentPropertyName);
		Set<String> directDependencies = getPropertyContext(dependentPropertyName).getDependencyPropertyNames();
		if (directDependencies.contains(dependencyPropertyName))
			return true;
		for (String directDependency: directDependencies) {
			if (hasTransitiveDependency(directDependency, dependencyPropertyName, checkedPropertyNames))
				return true;
		}
		return false;
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof PropertyUpdating) {
			event.stop();
			PropertyUpdating propertyUpdating = (PropertyUpdating) event.getPayload();
			List<PropertyContainer> propertyContainers = new ArrayList<>();
			for (Component item: propertiesView)
				propertyContainers.add((PropertyContainer) item);
			for (PropertyContainer propertyContainer: propertyContainers) {
				int propertyIndex = (int) propertyContainer.getDefaultModelObject();
				PropertyContext<Serializable> propertyContext = propertyContexts.get(propertyIndex);
				Set<String> checkedPropertyNames = new HashSet<>();
				if (hasTransitiveDependency(propertyContext.getPropertyName(), 
						propertyUpdating.getPropertyName(), checkedPropertyNames)) {
					Component newPropertyContainer = newItem(propertyContainer.getId(), propertyIndex);
					propertyContainer.replaceWith(newPropertyContainer);
					propertyUpdating.getHandler().add(newPropertyContainer);
				}
			}
			validate();
			if (!hasErrors(true)) 
				send(this, Broadcast.BUBBLE, new BeanUpdating(propertyUpdating.getHandler()));
			else
				clearErrors(true);
		}		
	}

	private WebMarkupContainer newItem(String id, int propertyIndex) {
		PropertyContext<Serializable> propertyContext = propertyContexts.get(propertyIndex);
		
		WebMarkupContainer item = new PropertyContainer(id, propertyIndex) {

			private Label descriptionLabel;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				setOutputMarkupPlaceholderTag(true);
				
				WebMarkupContainer nameContainer;
				WebMarkupContainer valueContainer;
				if (!vertical) {
					add(nameContainer = new WebMarkupContainer("name"));
					add(valueContainer = new WebMarkupContainer("value"));
				} else {
					nameContainer = this;
					valueContainer = this;
				}
				Label nameLabel = new Label("name", propertyContext.getDisplayName(this));
				nameContainer.add(nameLabel);
				
				OmitName omitName = propertyContext.getPropertyGetter().getAnnotation(OmitName.class);
				if (omitName != null && omitName.value() != OmitName.Place.VIEWER) {
					if (!vertical) {
						nameContainer.setVisible(false);
						valueContainer.add(AttributeAppender.replace("colspan", "2"));
					} else {
						nameLabel.setVisible(false);
					}
				}

				String required;
				if (propertyContext.isPropertyRequired() 
						&& propertyContext.getPropertyClass() != boolean.class
						&& propertyContext.getPropertyClass() != Boolean.class
						&& propertyContext.getPropertyClass() != List.class) {
					required = "*";
				} else {
					required = "&nbsp;";
				}
				
				nameContainer.add(new Label("required", required).setEscapeModelStrings(false));

				Serializable propertyValue;		
				
				OneContext context = new ComponentContext(this);
				
				OneContext.push(context);
				try {
					DefaultValueProvider defaultValueProvider = propertyContext.getPropertyGetter().getAnnotation(DefaultValueProvider.class);
					if (defaultValueProvider != null) {
						propertyValue = (Serializable) ReflectionUtils.invokeStaticMethod(
								propertyContext.getBeanClass(), defaultValueProvider.value());
					} else { 
						propertyValue = (Serializable) propertyContext.getPropertyValue(getModelObject());
					}
				} finally {
					OneContext.pop();
				}
				PropertyEditor<Serializable> propertyEditor = propertyContext.renderForEdit("value", Model.of(propertyValue)); 
				valueContainer.add(propertyEditor);
				
				descriptionLabel = new Label("description", propertyContext.getDescription(this)) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						if (StringUtils.isNotBlank(getModelValue())) {
							/*
							 * Hide the description when bean editor is in-place as we do not want to confuse the bean editor 
							 * property help with this help
							 */
							if (propertyEditor instanceof PolymorphicPropertyEditor || propertyEditor instanceof BeanPropertyEditor) {
								BeanEditor childBeanEditor = propertyEditor.visitChildren(BeanEditor.class, new IVisitor<BeanEditor, BeanEditor>() {

									@Override
									public void component(BeanEditor object, IVisit<BeanEditor> visit) {
										visit.stop(object);
									}

								});
								setVisible(childBeanEditor == null || !childBeanEditor.isVisible());
							} else {
								setVisible(true);
							}
						} else {
							setVisible(false);
						}
					}
					
				};
				descriptionLabel.setEscapeModelStrings(false);
				descriptionLabel.setOutputMarkupPlaceholderTag(true);
				valueContainer.add(descriptionLabel);
				
				valueContainer.add(new FencedFeedbackPanel("feedback", propertyEditor));
				
				valueContainer.add(AttributeAppender.append("class", "property-" + propertyContext.getPropertyName()));
			}

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof PropertyUpdating)
					((PropertyUpdating)event.getPayload()).getHandler().add(descriptionLabel);
			}

			@Override
			public Object getInputValue(String name) {
				/*
				 * Field will be be display name of the property when the bean class being edited is 
				 * generated via groovy script    
				 */
				String propertyName = beanDescriptor.getPropertyName(name);
				propertyContext.getDependencyPropertyNames().add(propertyName);

				Optional<Object> result= BeanEditor.this.visitChildren(PropertyEditor.class, new IVisitor<PropertyEditor<?>, Optional<Object>>() {

					@Override
					public void component(PropertyEditor<?> object, IVisit<Optional<Object>> visit) {
						if (object.getPropertyDescriptor().getPropertyName().equals(propertyName)) {
							visit.stop(Optional.ofNullable(object.getConvertedInput()));
						} else { 
							visit.dontGoDeeper();
						}
					}
					
				});
				if (result == null)
					return getPropertyContext(propertyName).getPropertyValue(getModelObject());
				else
					return result.orElse(null);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(propertyContext.isPropertyVisible(new ComponentContext(this), beanDescriptor) && !propertyContext.isExcluded());
			}

		};

		return item;
	}

	public PropertyContext<Serializable> getPropertyContext(String propertyName) {
		for (PropertyContext<Serializable> propertyContext: propertyContexts) {
			if (propertyContext.getPropertyName().equals(propertyName))
				return propertyContext;
		}
		throw new RuntimeException("Property not found: " + propertyName);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Fragment fragment;
		if (vertical) {
			fragment = new Fragment("content", "verticalFrag", this);
			fragment.add(AttributeAppender.append("class", " vertical"));
		} else {
			fragment = new Fragment("content", "horizontalFrag", this);
			fragment.add(AttributeAppender.append("class", " horizontal"));
		}
		
		add(fragment);
		
		propertiesView = new RepeatingView("properties");
		fragment.add(propertiesView);
		
		for (int i=0; i<propertyContexts.size(); i++) {
			propertiesView.add(newItem(propertiesView.newChildId(), i));
		}
		
		add(new IValidator<Serializable>() {

			@Override
			public void validate(IValidatable<Serializable> validatable) {
				OneContext.push(new ComponentContext(BeanEditor.this));
				try {
					Validator validator = AppLoader.getInstance(Validator.class);
					for (ConstraintViolation<Serializable> violation: validator.validate(validatable.getValue())) {
						ValuePath valuePath = new ValuePath(violation.getPropertyPath());
						if (!valuePath.getElements().isEmpty()) {
							PathSegment.Property property = (Property) valuePath.getElements().iterator().next();
							boolean found = false;
							for (Component item: propertiesView) {
								int propertyIndex = (int) item.getDefaultModelObject();
								PropertyContext<Serializable> propertyContext = propertyContexts.get(propertyIndex); 
								if (propertyContext.getPropertyName().equals(property.getName()) 
										&& propertyContext.isPropertyVisible(new ComponentContext(item), beanDescriptor)
										&& !propertyContext.isExcluded()) {
									found = true;
									break;
								}
							}
							if (!found)
								continue;
						}
						ErrorContext errorContext = getErrorContext(valuePath);
						errorContext.addError(violation.getMessage());
					}
				} finally {
					OneContext.pop();
				}
			}
			
		});
		
		add(AttributeAppender.append("class", " bean editor editable"));
		
		setOutputMarkupId(true);
	}
	
	public BeanDescriptor getBeanDescriptor() {
		return beanDescriptor;
	}

	public List<PropertyContext<Serializable>> getPropertyContexts() {
		return propertyContexts;
	}
	
	public OneContext getOneContext(String propertyName) {
		for (Component item: propertiesView) {
			int propertyIndex = (int) item.getDefaultModelObject();
			PropertyContext<Serializable> propertyContext = propertyContexts.get(propertyIndex); 
			if (propertyContext.getPropertyName().equals(propertyName)) 
				return new ComponentContext(item);
		}
		throw new RuntimeException("Property not found: " + propertyName);
	}
	
	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		final PathSegment.Property property = (Property) pathSegment;
		return visitChildren(PropertyEditor.class, new IVisitor<PropertyEditor<Serializable>, PropertyEditor<Serializable>>() {

			@Override
			public void component(PropertyEditor<Serializable> object, IVisit<PropertyEditor<Serializable>> visit) {
				if (object.getPropertyDescriptor().getPropertyName().equals(property.getName()))
					visit.stop(object);
				else
					visit.dontGoDeeper();
			}
			
		});
	}

	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		final Serializable bean = (Serializable) getBeanDescriptor().newBeanInstance();
		
		visitChildren(PropertyEditor.class, new IVisitor<PropertyEditor<Serializable>, PropertyEditor<Serializable>>() {

			@Override
			public void component(PropertyEditor<Serializable> object, IVisit<PropertyEditor<Serializable>> visit) {
				if (!object.getPropertyDescriptor().isExcluded())
					object.getPropertyDescriptor().setPropertyValue(bean, object.getConvertedInput());
				visit.dontGoDeeper();
			}
			
		});
		
		return bean;
	}
	
	private abstract class PropertyContainer extends WebMarkupContainer implements EditContext {

		public PropertyContainer(String id, int propertyIndex) {
			super(id, Model.of(propertyIndex));
		}

		@Override
		public void renderHead(IHeaderResponse response) {
			super.renderHead(response);
			
			response.render(JavaScriptHeaderItem.forReference(new PropertyContainerResourceReference()));
			
			String script = String.format("onedev.server.propertyContainer.onDomReady('%s');", getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}

	}
	
}
