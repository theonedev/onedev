package io.onedev.server.web.editable.polymorphiclist;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import io.onedev.server.annotation.ExcludedProperties;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;

public class PolymorphicListPropertyViewer extends Panel {

	private final List<Serializable> elements;
	
	private final Set<String> excludedProperties = new HashSet<>();
	
	public PolymorphicListPropertyViewer(String id, PropertyDescriptor descriptor, List<Serializable> elements) {
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

				item.add(new Label("type", EditableUtils.getDisplayName(value.getClass())));
				item.add(new Label("typeDescription", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return EditableUtils.getDescription(value.getClass());
					}

				}) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(EditableUtils.getDescription(value.getClass()) != null);
					}

				});
				item.add(BeanContext.view("beanViewer", value, excludedProperties, true));
			}

		});

		WebMarkupContainer noRecords = new WebMarkupContainer("noRecords");
		noRecords.setVisible(elements.isEmpty());
		table.add(noRecords);
	}

}
