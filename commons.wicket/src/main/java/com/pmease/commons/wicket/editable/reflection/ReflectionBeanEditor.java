package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.editable.annotation.OmitNames;
import com.pmease.commons.editable.annotation.TableLayout;
import com.pmease.commons.wicket.editable.EditableResourceBehavior;

@SuppressWarnings("serial")
public class ReflectionBeanEditor extends Panel {

	private final ReflectionBeanEditContext editContext;
	
	public ReflectionBeanEditor(String panelId, ReflectionBeanEditContext editContext) {
		super(panelId);
		
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new EditableResourceBehavior());
		
		add(new ListView<String>("beanValidationErrors", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return editContext.getValidationErrors();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("beanValidationError", item.getModelObject()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getModelObject().isEmpty());
			}
			
		});

		Fragment fragment;
		if (editContext.getBeanClass().getAnnotation(TableLayout.class) == null)
			fragment = new Fragment("beanEditor", "default", ReflectionBeanEditor.this);
		else
			fragment = new Fragment("beanEditor", "table", ReflectionBeanEditor.this);
		
		add(fragment);
		
		fragment.add(new ListView<PropertyEditContext>("properties", editContext.getPropertyContexts()) {

			@Override
			protected void populateItem(ListItem<PropertyEditContext> item) {
				final PropertyEditContext propertyContext = item.getModelObject();
				
				item.add(new Label("name", EditableUtils.getName(propertyContext.getPropertyGetter()))
						.setVisible(editContext.getBeanClass().getAnnotation(OmitNames.class) == null));

				String required;
				if (propertyContext.isPropertyRequired())
					required = "*";
				else
					required = "&nbsp;";
				
				item.add(new Label("required", required).setEscapeModelStrings(false));
				
				item.add((Component)propertyContext.renderForEdit("value"));
				
				WebMarkupContainer hint = new WebMarkupContainer("hint");
				String description = EditableUtils.getDescription(propertyContext.getPropertyGetter());
				if (description != null)
					hint.add(new Label("description", description).setEscapeModelStrings(false));
				else
					hint.add(new Label("description").setVisible(false));
				
				hint.add(new ListView<String>("propertyValidationErrors", propertyContext.getValidationErrors()) {

					@Override
					protected void populateItem(ListItem<String> item) {
						item.add(new Label("propertyValidationError", item.getModelObject()));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(!propertyContext.getValidationErrors().isEmpty());
					}
					
				});
				
				item.add(hint);
				
				Map<Serializable, EditContext> childContexts = propertyContext.getChildContexts();
				if (!propertyContext.getValidationErrors().isEmpty()) {
	                if (childContexts.isEmpty())
	                    item.add(AttributeModifier.append("class", "has-error"));
	                else
	                    hint.add(AttributeModifier.append("class", "has-error"));
				}
			}

		});
		
	}

}
