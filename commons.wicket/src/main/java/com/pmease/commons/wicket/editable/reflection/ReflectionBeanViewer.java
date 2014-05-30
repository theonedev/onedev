package com.pmease.commons.wicket.editable.reflection;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.editable.OmitName;
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class ReflectionBeanViewer extends Panel {

	private final ReflectionBeanEditContext editContext;
	
	public ReflectionBeanViewer(String panelId, ReflectionBeanEditContext editContext) {
		super(panelId);
		
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PropertyEditContext>("properties", editContext.getPropertyContexts()) {

			@Override
			protected void populateItem(ListItem<PropertyEditContext> item) {
				Method propertyGetter = item.getModelObject().getPropertyGetter();
				Label nameLabel = new Label("name", EditableUtils.getName(propertyGetter));
				item.add(nameLabel);
				OmitName omitName = propertyGetter.getAnnotation(OmitName.class);

				if (omitName != null && omitName.value() != OmitName.Place.EDITOR) {
					nameLabel.setVisible(false);
					item.add(AttributeAppender.append("class", "name-omitted"));
				}

				item.add((Component)item.getModelObject().renderForView("value"));
			}

		});
	}

}
