package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.HashMap;
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
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.PathNode.Named;
import io.onedev.server.web.editable.annotation.OmitName;

@SuppressWarnings("serial")
public class BeanEditor extends ValueEditor<Serializable> {

	public static final String SCRIPT_CONTEXT_BEAN = "beanEditor";
	
	private final BeanDescriptor descriptor;
	
	private final Map<String, List<PropertyContext<Serializable>>> propertyContexts = new LinkedHashMap<>();
	
	private RepeatingView groupsView;
	
	private Map<String, ComponentContext> componentContexts = new HashMap<>();
	
	public BeanEditor(String id, BeanDescriptor descriptor, IModel<Serializable> model) {
		super(id, model);
		
		this.descriptor = descriptor;
		
		for (Map.Entry<String, List<PropertyDescriptor>> entry: descriptor.getProperties().entrySet()) {
			propertyContexts.put(entry.getKey(), 
					entry.getValue().stream().map(it->PropertyContext.of(it)).collect(Collectors.toList()));
		}
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
				RepeatingView propertiesView = (RepeatingView) groupContainer.get("properties");
				for (Component propertyContainer: propertiesView) {
					@SuppressWarnings("unchecked")
					PropertyContext<Serializable> propertyContext = 
							(PropertyContext<Serializable>) propertyContainer.getDefaultModelObject(); 
					Set<String> checkedPropertyNames = new HashSet<>();
					if (hasTransitiveDependency(propertyContext.getPropertyName(), 
							propertyUpdating.getPropertyName(), checkedPropertyNames)) {
						/*
						 * Create new property container instead of simply refreshing it as some dependent 
						 * properties may only take effect when re-create the property container. For instance
						 * If default value of an issue field depends on input value of another issue field  
						 */
						PropertyContainer newPropertyContainer = 
								newPropertyContainer(propertyContainer.getId(), propertyContext);
						propertyContainer.replaceWith(newPropertyContainer);
						componentContexts.put(propertyContext.getPropertyName(), 
								new ComponentContext(newPropertyContainer));
						propertyUpdating.getHandler().add(newPropertyContainer);
						String script = String.format("$('#%s').addClass('no-autofocus');", 
								newPropertyContainer.getMarkupId());
						propertyUpdating.getHandler().appendJavaScript(script);
					}
				}
			}				
			
			convertInput();
			clearErrors();
			/**
			 * Bump up event even if some properties are invalid as we may need to do something with 
			 * partial properties of the bean. For instance to update issue description template
			 */
			send(this, Broadcast.BUBBLE, new BeanUpdating(propertyUpdating.getHandler()));
		}		
	}

	private PropertyContainer newPropertyContainer(String id, PropertyContext<Serializable> property) {
		PropertyContainer propertyContainer = new PropertyContainer(id, property) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				setOutputMarkupPlaceholderTag(true);
				
				Label nameLabel = new Label("name", property.getDescriptor().getDisplayName());
				add(nameLabel);
				
				OmitName omitName = property.getPropertyGetter().getAnnotation(OmitName.class);
				if (omitName != null && omitName.value() != OmitName.Place.VIEWER) 
					nameLabel.setVisible(false);

				String required;
				if (property.isPropertyRequired() 
						&& property.getPropertyClass() != boolean.class
						&& property.getPropertyClass() != Boolean.class) {
					required = "*";
				} else {
					required = "&nbsp;";
				}
				
				add(new Label("required", required).setEscapeModelStrings(false));

				Serializable propertyValue;		
				
				ComponentContext context = new ComponentContext(this);
				
				ComponentContext.push(context);
				try {
					propertyValue = (Serializable) property.getDescriptor().getPropertyValue(getModelObject());
				} finally {
					ComponentContext.pop();
				}
				PropertyEditor<Serializable> propertyEditor = property.renderForEdit("value", Model.of(propertyValue)); 
				add(propertyEditor);
				
				Label descriptionLabel = new Label("description", property.getDescriptor().getDescription());
				descriptionLabel.setEscapeModelStrings(false);
				descriptionLabel.setOutputMarkupPlaceholderTag(true);
				add(descriptionLabel);
				
				add(new FencedFeedbackPanel("feedback", propertyEditor));
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
						&& property.getDescriptor().isPropertyVisible(componentContexts, descriptor));
			}

		};
		
		propertyContainer.add(AttributeAppender.append("class", "property-" + property.getPropertyName()));

		return propertyContainer;
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
			if (entry.getKey().length() != 0) {
				groupContainer.add(AttributeAppender.append(
						"class", 
						"group-" + entry.getKey().replace(" ", "-").toLowerCase()));
			}
			
			WebMarkupContainer toggleLink = new WebMarkupContainer("toggle");
			toggleLink.add(new Label("groupName", entry.getKey()));
			groupContainer.add(toggleLink);
			
			RepeatingView propertiesView = new RepeatingView("properties");
			groupContainer.add(propertiesView);
			
			for (PropertyContext<Serializable> property: entry.getValue()) {
				PropertyContainer propertyContainer = newPropertyContainer(propertiesView.newChildId(), property);
				propertiesView.add(propertyContainer);
				componentContexts.put(property.getPropertyName(), new ComponentContext(propertyContainer));
			}
			
			if (entry.getKey().length() == 0) {
				toggleLink.setVisible(false);
				groupContainer.add(AttributeAppender.append("class", "expanded"));
			}
			groupsView.add(groupContainer);
		}
		
		add(new IValidator<Serializable>() {

			@Override
			public void validate(IValidatable<Serializable> validatable) {
				ComponentContext.push(newComponentContext());
				try {
					Validator validator = AppLoader.getInstance(Validator.class);
					for (ConstraintViolation<Serializable> violation: validator.validate(validatable.getValue()))
						error(new Path(violation.getPropertyPath()), violation.getMessage());
				} finally {
					ComponentContext.pop();
				}
			}
			
		});
		
		add(AttributeAppender.append("class", "bean-editor editable"));
		
		setOutputMarkupId(true);
	}
	
	public BeanDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public void error(PathNode propertyNode, Path pathInProperty, String errorMessage) {
		PathNode.Named named = (Named) propertyNode;
		PropertyEditor<?> propertyEditor = visitChildren(PropertyEditor.class, 
				new IVisitor<PropertyEditor<Serializable>, PropertyEditor<Serializable>>() {

			@Override
			public void component(PropertyEditor<Serializable> object, IVisit<PropertyEditor<Serializable>> visit) {
				if (object.getDescriptor().getPropertyName().equals(named.getName()) && object.isVisibleInHierarchy())
					visit.stop(object);
				else
					visit.dontGoDeeper();
			}
			
		});
		if (propertyEditor != null)
			propertyEditor.error(pathInProperty, errorMessage);
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
	
	public ComponentContext newComponentContext() {
		return new ComponentContext(this) {

			@Override
			public ComponentContext getChildContext(String childName) {
				for (Component groupContainer: groupsView) {
					RepeatingView propertiesView = (RepeatingView) groupContainer.get("properties");
					for (Component item: propertiesView) {
						@SuppressWarnings("unchecked")
						PropertyContext<Serializable> propertyContext = (PropertyContext<Serializable>) item.getDefaultModelObject(); 
						if (propertyContext.getPropertyName().equals(childName))
							return new ComponentContext(item);
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

	}
	
}
