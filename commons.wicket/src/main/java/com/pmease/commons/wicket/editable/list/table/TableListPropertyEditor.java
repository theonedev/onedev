package com.pmease.commons.wicket.editable.list.table;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.editable.ValidationError;
import com.pmease.commons.wicket.editable.RenderableEditContext;

@SuppressWarnings("serial")
public class TableListPropertyEditor extends Panel {

	private final TableListPropertyEditContext editContext;
	
	public TableListPropertyEditor(String id, TableListPropertyEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (editContext.isPropertyRequired()) {
			add(new WebMarkupContainer("enable").setVisible(false));
			if (editContext.getPropertyValue() == null) {
				editContext.setPropertyValue(editContext.instantiate(editContext.getPropertyGetter().getReturnType()));
			}
		} else {
			add(new CheckBox("enable", new IModel<Boolean>() {
				
				@Override
				public void detach() {
					
				}
	
				@Override
				public Boolean getObject() {
					return editContext.getPropertyValue() != null;
				}
	
				@Override
				public void setObject(Boolean object) {
					if (object) {
						editContext.setPropertyValue(editContext.instantiate(ArrayList.class));
					} else {
						editContext.setPropertyValue(null);
					}
				}
				
			}).add(new AjaxFormComponentUpdatingBehavior("onclick"){

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					Component editor = newPropertyValueEditor();
					replace(editor);
					target.add(editor);
				}
				
			}));
			
		}
		
		add(newPropertyValueEditor());
	}

	private Component newPropertyValueEditor() {
		final WebMarkupContainer table = new WebMarkupContainer("valueEditor") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(editContext.getElementContexts() != null);
			}
			
		};

		table.setOutputMarkupId(true);
		table.setOutputMarkupPlaceholderTag(true);
		
		table.add(new ListView<Method>("headers", new LoadableDetachableModel<List<Method>>() {

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
		table.add(new ListView<List<PropertyEditContext>>("elements", editContext.getElementContexts()) {

			@Override
			protected void populateItem(final ListItem<List<PropertyEditContext>> rowItem) {
				rowItem.add(new ListView<PropertyEditContext>("elementProperties", rowItem.getModelObject()) {

					@Override
					protected void populateItem(ListItem<PropertyEditContext> columnItem) {
						final PropertyEditContext elementPropertyContext = columnItem.getModelObject();
						columnItem.add(((RenderableEditContext)elementPropertyContext).renderForEdit("elementPropertyEditor"));
						
						columnItem.add(new ListView<ValidationError>("propertyValidationErrors", elementPropertyContext.getValidationErrors()) {

							@Override
							protected void populateItem(ListItem<ValidationError> item) {
								ValidationError error = item.getModelObject();
								item.add(new Label("propertyValidationError", error.toString()));
							}

							@Override
							protected void onConfigure() {
								super.onConfigure();
								
								setVisible(!elementPropertyContext.getValidationErrors().isEmpty());
							}
							
						});
						
					}
					
				});
				rowItem.add(new AjaxLink<Void>("deleteElement") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						editContext.removeElement(rowItem.getIndex());
						target.add(table);
					}
					
				});
			}
			
		});
		
		WebMarkupContainer newRow = new WebMarkupContainer("addElement");
		newRow.add(AttributeModifier.append("colspan", editContext.getElementPropertyGetters().size()));
		newRow.add(new AjaxLink<Void>("addElement") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editContext.addElement(editContext.getElementContexts().size(), 
						editContext.instantiate(editContext.getElementClass()));
				target.add(table);
			}
			
		});
		
		table.add(newRow);
		
		return table;		
	}
}
