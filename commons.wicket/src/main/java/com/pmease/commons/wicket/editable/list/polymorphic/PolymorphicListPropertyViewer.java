package com.pmease.commons.wicket.editable.list.polymorphic;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.wicket.editable.BeanContext;

@SuppressWarnings("serial")
public class PolymorphicListPropertyViewer extends Panel {

	private final List<Serializable> elements;
	
	public PolymorphicListPropertyViewer(String id, List<Serializable> elements) {
		super(id);
		this.elements = elements;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Serializable>("elements", elements) {

			@Override
			protected void populateItem(ListItem<Serializable> item) {
				item.add(BeanContext.view("element", item.getModelObject()));
			}
			
		});
		add(new WebMarkupContainer("noElements").setVisible(elements.isEmpty()));
	}

}
