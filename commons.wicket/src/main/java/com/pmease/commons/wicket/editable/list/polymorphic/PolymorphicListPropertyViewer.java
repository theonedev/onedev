package com.pmease.commons.wicket.editable.list.polymorphic;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.wicket.editable.BeanEditContext;

@SuppressWarnings("serial")
public class PolymorphicListPropertyViewer extends Panel {

	private final PolymorphicListPropertyEditConext editContext;
	
	public PolymorphicListPropertyViewer(String id, PolymorphicListPropertyEditConext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<BeanEditContext>("elements", editContext.getElementContexts()) {

			@Override
			protected void populateItem(ListItem<BeanEditContext> item) {
				item.add((Component)item.getModelObject().renderForView("element"));
			}
			
		});
		add(new WebMarkupContainer("noElements") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(editContext.getElementContexts().isEmpty());
			}
			
		});
	}

}
