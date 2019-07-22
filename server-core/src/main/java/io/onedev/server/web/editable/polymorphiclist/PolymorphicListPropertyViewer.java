package io.onedev.server.web.editable.polymorphiclist;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.annotation.ExcludedProperties;

@SuppressWarnings("serial")
public class PolymorphicListPropertyViewer extends Panel {

	private final List<Serializable> elements;
	
	private final Set<String> excludedProperties = new HashSet<>();
	
	public PolymorphicListPropertyViewer(String id, PropertyDescriptor descriptor, List<Serializable> elements) {
		super(id);
		this.elements = elements;
		
		ExcludedProperties excludedPropertiesAnnotation = 
				descriptor.getPropertyGetter().getAnnotation(ExcludedProperties.class);
		if (excludedPropertiesAnnotation != null) {
			for (String each: excludedPropertiesAnnotation.value())
				excludedProperties.add(each);
		}
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
				item.add(BeanContext.view("element", item.getModelObject(), excludedProperties, true));
			}
			
		});
		add(new WebMarkupContainer("noElements").setVisible(elements.isEmpty()));
	}

}
