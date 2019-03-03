package io.onedev.server.web.editable.polymorphiclist;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;

@SuppressWarnings("serial")
public class PolymorphicListPropertyViewer extends Panel {

	private final List<Serializable> elements;
	
	public PolymorphicListPropertyViewer(String id, PropertyDescriptor propertyDescriptor, List<Serializable> elements) {
		super(id);
		this.elements = elements;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Serializable>("elements", elements) {

			@Override
			protected void populateItem(ListItem<Serializable> item) {
				String displayName = EditableUtils.getDisplayName(item.getModelObject().getClass());
				displayName = Application.get().getResourceSettings().getLocalizer().getString(displayName, this, displayName);
				item.add(new Label("elementType", displayName));
				item.add(BeanContext.viewBean("element", item.getModelObject()));
			}
			
		});
		add(new WebMarkupContainer("noElements").setVisible(elements.isEmpty()));
	}

}
