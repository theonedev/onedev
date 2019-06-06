package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
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

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.PathElement.Named;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.Vertical;

@SuppressWarnings("serial")
public class BeanEditor extends ValueEditor<Serializable> {

	public static final String SCRIPT_CONTEXT_BEAN = "beanEditor";
	
	private final BeanDescriptor descriptor;
	
	private final Map<String, List<PropertyContext<Serializable>>> propertyContexts = new LinkedHashMap<>();
	
	private final boolean vertical;
	
	private RepeatingView groupsView;
	
	public BeanEditor(String id, BeanDescriptor descriptor, IModel<Serializable> model) {
		super(id, model);
		
		this.descriptor = descriptor;
		
		for (Map.Entry<String, List<PropertyDescriptor>> entry: descriptor.getProperties().entrySet()) {
			propertyContexts.put(entry.getKey(), 
					entry.getValue().stream().map(it->PropertyContext.of(it)).collect(Collectors.toList()));
		}
		
		Class<?> beanClass = descriptor.getBeanClass();
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
		Set<String> directDependencies = getPropertyContext(dependentPropertyName).getDescriptor().getDependencyPropertyNames();
		if (directDependencies.contains(dependencyPropertyName))
			return true;
		for (String directDependency: directDependencies) {
			if (hasTransitiveDependency(directDependency, dependencyPropertyName, new HashSet<>(checkedPropertyNames)))
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
			for (Component groupContainer: groupsView) {
				RepeatingView propertiesView = (RepeatingView) groupContainer.get("content").get("properties");
				for (Component item: propertiesView) {
					@SuppressWarnings("unchecked")
					PropertyContext<Serializable> propertyContext = (PropertyContext<Serializable>) item.getDefaultModelObject(); 
					Set<String> checkedPropertyNames = new HashSet<>();
					if (hasTransitiveDependency(propertyContext.getPropertyName(), 
							propertyUpdating.getPropertyName(), checkedPropertyNames)) {
						propertyUpdating.getHandler().add(item);
						String script = String.format("$('#%s').addClass('no-autofocus');", item.getMarkupId());
						propertyUpdating.getHandler().appendJavaScript(script);
					}
				}
			}				
			validate();
			if (!hasErrors(true)) 
				send(this, Broadcast.BUBBLE, new BeanUpdating(propertyUpdating.getHandler()));
			else
				clearErrors(true);
		}		
	}

	private WebMarkupContainer newItem(String id, PropertyContext<Serializable> property) {
		WebMarkupContainer item = new PropertyContainer(id, property) {

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
				Label nameLabel = new Label("name", property.getDescriptor().getDisplayName(this));
				nameContainer.add(nameLabel);
				
				OmitName omitName = property.getPropertyGetter().getAnnotation(OmitName.class);
				if (omitName != null && omitName.value() != OmitName.Place.VIEWER) {
					if (!vertical) {
						nameContainer.setVisible(false);
						valueContainer.add(AttributeAppender.replace("colspan", "2"));
					} else {
						nameLabel.setVisible(false);
					}
				}

				String required;
				if (property.isPropertyRequired() 
						&& property.getPropertyClass() != boolean.class
						&& property.getPropertyClass() != Boolean.class) {
					required = "*";
				} else {
					required = "&nbsp;";
				}
				
				nameContainer.add(new Label("required", required).setEscapeModelStrings(false));

				Serializable propertyValue;		
				
				OneContext context = new OneContext(this);
				
				OneContext.push(context);
				try {
					propertyValue = (Serializable) property.getDescriptor().getPropertyValue(getModelObject());
				} finally {
					OneContext.pop();
				}
				PropertyEditor<Serializable> propertyEditor = property.renderForEdit("value", Model.of(propertyValue)); 
				valueContainer.add(propertyEditor);
				
				descriptionLabel = new Label("description", property.getDescriptor().getDescription(this)) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(StringUtils.isNotBlank(getModelValue()));
					}
					
				};
				descriptionLabel.setEscapeModelStrings(false);
				descriptionLabel.setOutputMarkupPlaceholderTag(true);
				valueContainer.add(descriptionLabel);
				
				valueContainer.add(new FencedFeedbackPanel("feedback", propertyEditor));
				
				valueContainer.add(AttributeAppender.append("class", "property-" + property.getPropertyName()));
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
				 * Field will be display name of the property when the bean class being edited is 
				 * generated via groovy script    
				 */
				String propertyName = descriptor.getPropertyName(name);
				property.getDescriptor().getDependencyPropertyNames().add(propertyName);
				
				Optional<Object> result= BeanEditor.this.visitChildren(PropertyEditor.class, new IVisitor<PropertyEditor<?>, Optional<Object>>() {

					@Override
					public void component(PropertyEditor<?> object, IVisit<Optional<Object>> visit) {
						if (object.getDescriptor().getPropertyName().equals(propertyName))
							visit.stop(Optional.ofNullable(object.getConvertedInput()));
						else  
							visit.dontGoDeeper();
					}
					
				});
				if (result == null)
					return getPropertyContext(propertyName).getDescriptor().getPropertyValue(getModelObject());
				else
					return result.orElse(null);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!property.getDescriptor().isPropertyExcluded() 
						&& property.getDescriptor().isPropertyVisible(new OneContext(this), descriptor));
			}

		};

		return item;
	}

	public PropertyContext<Serializable> getPropertyContext(String propertyName) {
		for (List<PropertyContext<Serializable>> groupProperties: propertyContexts.values()) {
			for (PropertyContext<Serializable> property: groupProperties) {
				if (property.getPropertyName().equals(propertyName))
					return property;
			}
		}
		throw new RuntimeException("Property not found: " + propertyName);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		groupsView = new RepeatingView("groups");
		add(groupsView);
		
		for (Map.Entry<String, List<PropertyContext<Serializable>>> entry: propertyContexts.entrySet()) {
			WebMarkupContainer groupContainer = new WebMarkupContainer(groupsView.newChildId());
			
			WebMarkupContainer toggleLink = new WebMarkupContainer("toggle");
			toggleLink.add(new Label("groupName", entry.getKey()));
			groupContainer.add(toggleLink);
			
			Fragment contentFrag;
			if (vertical) {
				contentFrag = new Fragment("content", "verticalPropertiesFrag", this);
				contentFrag.add(AttributeAppender.append("class", " vertical"));
			} else {
				contentFrag = new Fragment("content", "horizontalPropertiesFrag", this);
				contentFrag.add(AttributeAppender.append("class", " horizontal"));
			}
			RepeatingView propertiesView = new RepeatingView("properties");
			contentFrag.add(propertiesView);
			
			for (PropertyContext<Serializable> property: entry.getValue())
				propertiesView.add(newItem(propertiesView.newChildId(), property));
			
			groupContainer.add(contentFrag);
			
			if (entry.getKey().length() == 0) {
				toggleLink.setVisible(false);
				groupContainer.add(AttributeAppender.append("class", "expanded"));
			}
			groupsView.add(groupContainer);
		}
		
		add(new IValidator<Serializable>() {

			@Override
			public void validate(IValidatable<Serializable> validatable) {
				OneContext.push(getOneContext());
				try {
					Validator validator = AppLoader.getInstance(Validator.class);
					for (ConstraintViolation<Serializable> violation: validator.validate(validatable.getValue())) {
						ErrorContext errorContext = getErrorContext(new ValuePath(violation.getPropertyPath()));
						if (errorContext != null)
							errorContext.addError(violation.getMessage());
					}
				} finally {
					OneContext.pop();
				}
			}
			
		});
		
		add(AttributeAppender.append("class", "bean-editor editable"));
		
		if (vertical)
			add(AttributeAppender.append("class", "vertical"));
		else
			add(AttributeAppender.append("class", "horizontal"));
		
		setOutputMarkupId(true);
	}
	
	public BeanDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public ErrorContext getErrorContext(PathElement element) {
		PathElement.Named namedElement = (Named) element;
		return visitChildren(PropertyEditor.class, new IVisitor<PropertyEditor<Serializable>, PropertyEditor<Serializable>>() {

			@Override
			public void component(PropertyEditor<Serializable> object, IVisit<PropertyEditor<Serializable>> visit) {
				if (object.getDescriptor().getPropertyName().equals(namedElement.getName()) && object.isVisibleInHierarchy())
					visit.stop(object);
				else
					visit.dontGoDeeper();
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		String script = String.format("onedev.server.editable.onBeanEditorDomReady('%s');", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		final Serializable bean = (Serializable) getDescriptor().newBeanInstance();
		
		visitChildren(PropertyEditor.class, new IVisitor<PropertyEditor<Serializable>, PropertyEditor<Serializable>>() {

			@Override
			public void component(PropertyEditor<Serializable> object, IVisit<PropertyEditor<Serializable>> visit) {
				if (!object.getDescriptor().isPropertyExcluded()) 
					object.getDescriptor().setPropertyValue(bean, object.getConvertedInput());
				visit.dontGoDeeper();
			}
			
		});
		
		return bean;
	}
	
	public OneContext getOneContext() {
		return new OneContext(this) {

			@Override
			public OneContext getPropertyContext(String propertyName) {
				for (Component groupContainer: groupsView) {
					RepeatingView propertiesView = (RepeatingView) groupContainer.get("content").get("properties");
					for (Component item: propertiesView) {
						@SuppressWarnings("unchecked")
						PropertyContext<Serializable> propertyContext = (PropertyContext<Serializable>) item.getDefaultModelObject(); 
						if (propertyContext.getPropertyName().equals(propertyName))
							return new OneContext(item);
					}
				}
				return null;
			}
			
		};
	}
	
	private abstract class PropertyContainer extends WebMarkupContainer implements EditContext {

		public PropertyContainer(String id, PropertyContext<Serializable> property) {
			super(id, Model.of(property));
		}

		@Override
		public void renderHead(IHeaderResponse response) {
			super.renderHead(response);
			
			String script = String.format("onedev.server.editable.onBeanEditorPropertyContainerDomReady('%s');", getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}

	}
	
}
