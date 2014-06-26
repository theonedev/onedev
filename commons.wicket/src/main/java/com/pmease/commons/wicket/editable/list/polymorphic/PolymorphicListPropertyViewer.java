package com.pmease.commons.wicket.editable.list.polymorphic;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.commons.editable.annotation.Vertical;
import com.pmease.commons.wicket.editable.BeanContext;

@SuppressWarnings("serial")
public class PolymorphicListPropertyViewer extends Panel {

	private final List<Serializable> elements;
	
	private final boolean horizontal;
	
	public PolymorphicListPropertyViewer(String id, PropertyDescriptor propertyDescriptor, List<Serializable> elements) {
		super(id);
		this.elements = elements;
		
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
		if (propertyGetter.getAnnotation(Horizontal.class) != null)
			horizontal = true;
		else if (propertyGetter.getAnnotation(Vertical.class) != null)
			horizontal = false;
		else 
			horizontal = true;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer table = new WebMarkupContainer("table");
		if (horizontal)
			table.add(AttributeAppender.append("class", " horizontal"));
		else
			table.add(AttributeAppender.append("class", " vertical"));
		add(table);
		
		table.add(new ListView<Serializable>("elements", elements) {

			@Override
			protected void populateItem(ListItem<Serializable> item) {
				Fragment fragment;
				if (horizontal)
					fragment = new Fragment("content", "horizontalFrag", PolymorphicListPropertyViewer.this);
				else
					fragment = new Fragment("content", "verticalFrag", PolymorphicListPropertyViewer.this);
				item.add(fragment);
				
				fragment.add(new Label("elementType", EditableUtils.getName(item.getModelObject().getClass())));
				fragment.add(BeanContext.viewBean("element", item.getModelObject()));
			}
			
		});
		table.add(new WebMarkupContainer("noElements").setVisible(elements.isEmpty()));
	}

}
