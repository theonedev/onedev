package io.onedev.server.web.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.OmitName;

@SuppressWarnings("serial")
public class BeanViewer extends Panel implements EditContext {

	private final BeanDescriptor beanDescriptor;
	
	private final Map<String, List<PropertyContext<Serializable>>> propertyContexts = new LinkedHashMap<>();
	
	public BeanViewer(String id, BeanDescriptor beanDescriptor, IModel<Serializable> model) {
		super(id, model);
	
		this.beanDescriptor = beanDescriptor;
		
		for (Map.Entry<String, List<PropertyDescriptor>> entry: beanDescriptor.getPropertyDescriptors().entrySet()) {
			propertyContexts.put(entry.getKey(), 
					entry.getValue().stream().map(it->PropertyContext.of(it)).collect(Collectors.toList()));
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Map.Entry<String, List<PropertyContext<Serializable>>>>("groups", new LoadableDetachableModel<List<Map.Entry<String, List<PropertyContext<Serializable>>>>>() {

			@Override
			protected List<Entry<String, List<PropertyContext<Serializable>>>> load() {
				return new ArrayList<>(propertyContexts.entrySet());
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Entry<String, List<PropertyContext<Serializable>>>> item) {
				Entry<String, List<PropertyContext<Serializable>>> entry = item.getModelObject();
				
				WebMarkupContainer toggleLink = new WebMarkupContainer("toggle");
				toggleLink.add(new Label("groupName", entry.getKey()));
				item.add(toggleLink);

				if (entry.getKey().length() == 0) {
					toggleLink.setVisible(false);
					item.add(AttributeAppender.append("class", "expanded"));
				}
				
				item.add(new ListView<PropertyContext<Serializable>>("properties", entry.getValue()) {

					@Override
					protected void populateItem(ListItem<PropertyContext<Serializable>> item) {
						PropertyContext<Serializable> propertyContext = item.getModelObject();
						Method propertyGetter = propertyContext.getPropertyGetter();
						
						WebMarkupContainer nameTd = new WebMarkupContainer("name");
						item.add(nameTd);
						WebMarkupContainer valueTd = new WebMarkupContainer("value");
						item.add(valueTd);
						
						String displayName = propertyContext.getDisplayName(this);
						Component content = new Label("content", displayName);
						nameTd.add(content);
						OmitName omitName = propertyGetter.getAnnotation(OmitName.class);

						if (omitName != null && omitName.value() != OmitName.Place.EDITOR) {
							nameTd.setVisible(false);
							valueTd.add(AttributeAppender.replace("colspan", "2"));
							item.add(AttributeAppender.append("class", "name-omitted"));
						}

						Serializable bean = (Serializable) BeanViewer.this.getDefaultModelObject();
						Serializable propertyValue = (Serializable) propertyContext.getPropertyValue(bean);
						valueTd.add(propertyContext.renderForView("content", Model.of(propertyValue)));
						
						item.setVisible(propertyContext.isPropertyVisible(new OneContext(BeanViewer.this), beanDescriptor) 
								&& !propertyContext.isPropertyExcluded());
						
						item.add(AttributeAppender.append("class", "property-" + propertyContext.getPropertyName()));
					}

				});
			}
			
		});
		
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

	@Override
	public Object getInputValue(String name) {
		String propertyName = beanDescriptor.getPropertyName(name);
		for (List<PropertyContext<Serializable>> groupProperties: propertyContexts.values()) {
			for (PropertyContext<Serializable> property: groupProperties) {
				if (property.getPropertyName().equals(propertyName))
					return property.getPropertyValue(getDefaultModelObject());
			}
		}
		throw new RuntimeException("Property not found: " + propertyName);
	}

}
