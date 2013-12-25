package com.pmease.commons.wicket.editable.list.table;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class TableListPropertyViewer extends Panel {

	private final TableListPropertyEditContext editContext;
	
	public TableListPropertyViewer(String id, TableListPropertyEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Method>("headers", new LoadableDetachableModel<List<Method>>() {

			@Override
			protected List<Method> load() {
				return editContext.getElementPropertyGetters();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Method> item) {
				item.add(new Label("header", EditableUtils.getName(item.getModelObject())));
			}
			
		});
		add(new ListView<List<PropertyEditContext>>("rows", editContext.getElementContexts()) {

			@Override
			protected void populateItem(ListItem<List<PropertyEditContext>> rowItem) {
				rowItem.add(new ListView<PropertyEditContext>("columns", rowItem.getModelObject()) {

					@Override
					protected void populateItem(ListItem<PropertyEditContext> columnItem) {
						EditContext elementPropertyContext = columnItem.getModelObject();
						columnItem.add((Component)elementPropertyContext.renderForView("cell"));
					}
					
				});
			}
			
		});
		WebMarkupContainer noElements = new WebMarkupContainer("noElements") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(editContext.getElementContexts().isEmpty());
			}
			
		};
		noElements.add(AttributeModifier.append("colspan", editContext.getElementPropertyGetters().size()));
		add(noElements);
	}

}
