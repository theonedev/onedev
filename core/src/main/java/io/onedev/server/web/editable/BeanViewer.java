package io.onedev.server.web.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.util.ComponentContext;

@SuppressWarnings("serial")
public class BeanViewer extends Panel implements EditContext {

	private final BeanDescriptor beanDescriptor;
	
	private final List<PropertyContext<Serializable>> propertyContexts = new ArrayList<>();
	
	public BeanViewer(String id, BeanDescriptor beanDescriptor, IModel<Serializable> model) {
		super(id, model);
	
		this.beanDescriptor = beanDescriptor;
		
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
				
				item.setVisible(propertyContext.isPropertyVisible(new ComponentContext(BeanViewer.this), beanDescriptor) 
						&& !propertyContext.isPropertyExcluded());
				
				item.add(AttributeAppender.append("class", "property-" + propertyContext.getPropertyName()));
			}

		});
		
		add(AttributeAppender.append("class", "bean viewer editable"));
	}

	@Override
	public Object getInputValue(String name) {
		String propertyName = beanDescriptor.getPropertyName(name);
		for (PropertyContext<?> propertyContext: propertyContexts) {
			if (propertyContext.getPropertyName().equals(propertyName))
				return propertyContext.getPropertyValue(getDefaultModelObject());
		}
		throw new RuntimeException("Property not found: " + propertyName);
	}

}
