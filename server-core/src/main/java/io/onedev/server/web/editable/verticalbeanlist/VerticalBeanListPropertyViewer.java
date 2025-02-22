package io.onedev.server.web.editable.verticalbeanlist;

import io.onedev.server.annotation.ExcludedProperties;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class VerticalBeanListPropertyViewer extends Panel {

	private final List<Serializable> elements;
	
	private final Set<String> excludedProperties = new HashSet<>();
	
	public VerticalBeanListPropertyViewer(String id, PropertyDescriptor descriptor, List<Serializable> elements) {
		super(id);
		this.elements = elements;
		
		ExcludedProperties excludedPropertiesAnnotation = 
				descriptor.getPropertyGetter().getAnnotation(ExcludedProperties.class);
		if (excludedPropertiesAnnotation != null) 
			excludedProperties.addAll(asList(excludedPropertiesAnnotation.value()));
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
		
		table.add(new ListView<>("elements", elements) {

			@Override
			protected void populateItem(ListItem<Serializable> item) {
				var value = item.getModelObject();
				item.add(BeanContext.view("beanViewer", value, excludedProperties, true));
			}

		});

		WebMarkupContainer noRecords = new WebMarkupContainer("noRecords");
		noRecords.setVisible(elements.isEmpty());
		table.add(noRecords);
	}

}
