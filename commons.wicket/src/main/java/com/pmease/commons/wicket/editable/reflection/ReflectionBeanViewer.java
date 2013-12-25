package com.pmease.commons.wicket.editable.reflection;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.editable.annotation.OmitNames;

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
				item.add(new Label("name", EditableUtils.getName(item.getModelObject().getPropertyGetter()))
						.setVisible(editContext.getBeanClass().getAnnotation(OmitNames.class) == null));
				
				item.add((Component)item.getModelObject().renderForView("value"));
			}

		});
	}

}
