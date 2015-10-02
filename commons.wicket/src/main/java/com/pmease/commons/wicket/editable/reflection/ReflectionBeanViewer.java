package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.editable.BeanDescriptor;
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.annotation.OmitName;

@SuppressWarnings("serial")
public class ReflectionBeanViewer extends Panel {

	private final List<PropertyContext<Serializable>> propertyContexts = new ArrayList<>();
	
	public ReflectionBeanViewer(String id, BeanDescriptor beanDescriptor, IModel<Serializable> model) {
		super(id, model);
		
		for (PropertyDescriptor propertyDescriptor: beanDescriptor.getPropertyDescriptors()) {
			propertyContexts.add(PropertyContext.of(propertyDescriptor));
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PropertyContext<Serializable>>("properties", propertyContexts) {

			@Override
			protected void populateItem(ListItem<PropertyContext<Serializable>> item) {
				PropertyContext<Serializable> propertyContext = item.getModelObject();
				Method propertyGetter = propertyContext.getPropertyGetter();
				Label nameLabel = new Label("name", EditableUtils.getName(propertyGetter));
				item.add(nameLabel);
				OmitName omitName = propertyGetter.getAnnotation(OmitName.class);

				if (omitName != null && omitName.value() != OmitName.Place.EDITOR) {
					nameLabel.setVisible(false);
					item.add(AttributeAppender.append("class", "name-omitted"));
				}

				Serializable bean = (Serializable) ReflectionBeanViewer.this.getDefaultModelObject();
				Serializable propertyValue = (Serializable) propertyContext.getPropertyValue(bean);
				item.add(propertyContext.renderForView("value", Model.of(propertyValue)));
			}

		});
	}
}
