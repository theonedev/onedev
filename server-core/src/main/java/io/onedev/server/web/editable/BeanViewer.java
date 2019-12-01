package io.onedev.server.web.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.util.EditContext;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.editable.annotation.OmitName;

@SuppressWarnings("serial")
public class BeanViewer extends Panel {

	private final BeanDescriptor descriptor;
	
	private final Map<String, List<PropertyContext<Serializable>>> properties = new LinkedHashMap<>();
	
	public BeanViewer(String id, BeanDescriptor descriptor, IModel<Serializable> model) {
		super(id, model);
	
		this.descriptor = descriptor;
		
		for (Map.Entry<String, List<PropertyDescriptor>> entry: descriptor.getProperties().entrySet()) {
			properties.put(entry.getKey(), 
					entry.getValue().stream().map(it->PropertyContext.of(it)).collect(Collectors.toList()));
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Map<String, ComponentContext> componentContexts = new HashMap<>();
		RepeatingView groupsView = new RepeatingView("groups");
		for (Map.Entry<String, List<PropertyContext<Serializable>>> entry: properties.entrySet()) {
			WebMarkupContainer groupContainer = new WebMarkupContainer(groupsView.newChildId());
			if (entry.getKey().length() != 0) {
				groupContainer.add(AttributeAppender.append("class", 
						"group-" + entry.getKey().replace(" ", "-").toLowerCase()));
			}
			groupsView.add(groupContainer);
			WebMarkupContainer toggleLink = new WebMarkupContainer("toggle");
			toggleLink.add(new Label("groupName", entry.getKey()));
			groupContainer.add(toggleLink);

			if (entry.getKey().length() == 0) {
				toggleLink.setVisible(false);
				groupContainer.add(AttributeAppender.append("class", "expanded"));
			}
			
			RepeatingView propertiesView = new RepeatingView("properties");
			for (PropertyContext<Serializable> property: entry.getValue()) {
				WebMarkupContainer propertyContainer = new PropertyContainer(propertiesView.newChildId()) {

					@Override
					public Object getInputValue(String name) {
						String propertyName = descriptor.getPropertyName(name);
						property.getDescriptor().getDependencyPropertyNames().add(propertyName);
						for (List<PropertyContext<Serializable>> groupProperties: properties.values()) {
							for (PropertyContext<Serializable> property: groupProperties) {
								if (property.getPropertyName().equals(propertyName))
									return property.getPropertyValue(BeanViewer.this.getDefaultModelObject());
							}
						}
						throw new RuntimeException("Property not found: " + propertyName);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(property.isPropertyVisible(componentContexts, descriptor) && !property.isPropertyExcluded());
					}
					
				};
				propertiesView.add(propertyContainer);
				componentContexts.put(property.getPropertyName(), new ComponentContext(propertyContainer));
				
				Method propertyGetter = property.getPropertyGetter();
				
				WebMarkupContainer nameTd = new WebMarkupContainer("name");
				propertyContainer.add(nameTd);
				WebMarkupContainer valueTd = new WebMarkupContainer("value");
				propertyContainer.add(valueTd);
				
				String displayName = property.getDisplayName();
				Component content = new Label("content", displayName);
				nameTd.add(content);
				OmitName omitName = propertyGetter.getAnnotation(OmitName.class);

				if (omitName != null && omitName.value() != OmitName.Place.EDITOR) {
					nameTd.setVisible(false);
					valueTd.add(AttributeAppender.replace("colspan", "2"));
					propertyContainer.add(AttributeAppender.append("class", "name-omitted"));
				}

				Serializable bean = (Serializable) BeanViewer.this.getDefaultModelObject();
				Serializable propertyValue = (Serializable) property.getPropertyValue(bean);
				valueTd.add(property.renderForView("content", Model.of(propertyValue)));
				
				propertyContainer.add(AttributeAppender.append("class", "property-" + property.getPropertyName()));
			}
			groupContainer.add(propertiesView);
		}
		add(groupsView);
		
		add(AttributeAppender.append("class", "bean-viewer editable"));
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new EditableResourceReference()));
		String script = String.format("onedev.server.editable.onBeanViewerDomReady('%s');", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	private abstract class PropertyContainer extends WebMarkupContainer implements EditContext {

		public PropertyContainer(String id) {
			super(id);
		}

	}
}
