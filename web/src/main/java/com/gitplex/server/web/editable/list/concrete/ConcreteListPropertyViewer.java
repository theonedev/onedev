package com.gitplex.server.web.editable.list.concrete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.gitplex.server.util.editable.EditableUtils;
import com.gitplex.server.web.editable.BeanDescriptor;
import com.gitplex.server.web.editable.PropertyContext;
import com.gitplex.server.web.editable.PropertyDescriptor;

@SuppressWarnings("serial")
public class ConcreteListPropertyViewer extends Panel {

	private final List<PropertyContext<Serializable>> elementPropertyContexts;
	
	private final List<Serializable> elements;
	
	public ConcreteListPropertyViewer(String id, Class<?> elementClass, List<Serializable> elements) {
		super(id);
		
		elementPropertyContexts = new ArrayList<>();
		for (PropertyDescriptor propertyDescriptor: new BeanDescriptor(elementClass).getPropertyDescriptors()) {
			elementPropertyContexts.add(PropertyContext.of(propertyDescriptor));
		}
		
		this.elements = elements;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PropertyContext<Serializable>>("headers", elementPropertyContexts) {

			@Override
			protected void populateItem(ListItem<PropertyContext<Serializable>> item) {
				item.add(new Label("header", EditableUtils.getName(item.getModelObject().getPropertyGetter())));
			}
			
		});
		add(new ListView<Serializable>("rows", elements) {

			@Override
			protected void populateItem(final ListItem<Serializable> rowItem) {
				rowItem.add(new ListView<PropertyContext<Serializable>>("columns", elementPropertyContexts) {

					@Override
					protected void populateItem(ListItem<PropertyContext<Serializable>> columnItem) {
						PropertyContext<Serializable> propertyContext = columnItem.getModelObject(); 
						Serializable elementPropertyValue = (Serializable) propertyContext.getPropertyValue(rowItem.getModelObject());
						columnItem.add(propertyContext.renderForView("cell", Model.of(elementPropertyValue)));
					}
					
				});
			}
			
		});
		WebMarkupContainer noElements = new WebMarkupContainer("noElements");
		noElements.setVisible(elements.isEmpty());
		noElements.add(AttributeModifier.append("colspan", elementPropertyContexts.size()));
		add(noElements);
	}

}
