package io.onedev.server.web.editable.beanlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.annotation.ExcludedProperties;

@SuppressWarnings("serial")
public class BeanListPropertyViewer extends Panel {

	private final List<PropertyContext<Serializable>> propertyContexts;
	
	private final List<Serializable> elements;
	
	public BeanListPropertyViewer(String id, PropertyDescriptor descriptor, Class<?> elementClass, List<Serializable> elements) {
		super(id);
		
		Set<String> excludedProperties = new HashSet<>();
		ExcludedProperties excludedPropertiesAnnotation = 
				descriptor.getPropertyGetter().getAnnotation(ExcludedProperties.class);
		if (excludedPropertiesAnnotation != null) {
			for (String each: excludedPropertiesAnnotation.value())
				excludedProperties.add(each);
		}
		
		propertyContexts = new ArrayList<>();
		for (List<PropertyDescriptor> groupProperties: new BeanDescriptor(elementClass).getProperties().values()) {
			for (PropertyDescriptor property: groupProperties)
				propertyContexts.add(PropertyContext.of(property));
		}
		
		this.elements = elements;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer table = new WebMarkupContainer("table") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (elements.isEmpty()) 
					NoRecordsBehavior.decorate(tag);
			}
			
		};
		add(table);
		
		table.add(new ListView<PropertyContext<Serializable>>("headers", propertyContexts) {

			@Override
			protected void populateItem(ListItem<PropertyContext<Serializable>> item) {
				PropertyContext<?> propertyContext = item.getModelObject();
				item.add(new Label("header", EditableUtils.getDisplayName(propertyContext.getPropertyGetter())));
				item.add(AttributeAppender.append("class", "property-" + propertyContext.getPropertyName()));
			}
			
		});
		
		table.add(new ListView<Serializable>("rows", elements) {

			@Override
			protected void populateItem(final ListItem<Serializable> rowItem) {
				rowItem.add(new ListView<PropertyContext<Serializable>>("columns", propertyContexts) {

					@Override
					protected void populateItem(ListItem<PropertyContext<Serializable>> columnItem) {
						PropertyContext<Serializable> propertyContext = columnItem.getModelObject(); 
						Serializable elementPropertyValue = (Serializable) propertyContext.getPropertyValue(rowItem.getModelObject());
						columnItem.add(propertyContext.renderForView("cell", Model.of(elementPropertyValue)));
						columnItem.add(AttributeAppender.append("class", "property-" + propertyContext.getPropertyName()));
					}
					
				});
			}
			
		});
		WebMarkupContainer noRecords = new WebMarkupContainer("noRecords");
		noRecords.add(new WebMarkupContainer("td").add(AttributeModifier.append("colspan", propertyContexts.size())));
		noRecords.setVisible(elements.isEmpty());
		table.add(noRecords);
	}

}
