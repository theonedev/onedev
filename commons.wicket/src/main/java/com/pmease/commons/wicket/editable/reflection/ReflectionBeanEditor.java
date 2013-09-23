package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.editable.ValidationError;
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
		
		add(new ListView<ValidationError>("beanValidationErrors", new LoadableDetachableModel<List<ValidationError>>() {

			@Override
			protected List<ValidationError> load() {
				return editContext.getValidationErrors(false);
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<ValidationError> item) {
				ValidationError error = item.getModelObject();
				item.add(new Label("beanValidationError", error.toString()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getModelObject().isEmpty());
			}
			
		});

		Fragment fragment;
		if (editContext.getBean().getClass().getAnnotation(TableLayout.class) == null)
			fragment = new Fragment("beanEditor", "default", ReflectionBeanEditor.this);
		else
			fragment = new Fragment("beanEditor", "table", ReflectionBeanEditor.this);
		
		add(fragment);
		
		fragment.add(new ListView<PropertyEditContext>("properties", editContext.getPropertyContexts()) {

			@Override
			protected void populateItem(ListItem<PropertyEditContext> item) {
				final PropertyEditContext propertyContext = item.getModelObject();
				
				item.add(new Label("name", EditableUtils.getName(propertyContext.getPropertyGetter())));

				String required;
				if (propertyContext.isPropertyRequired())
					required = "*";
				else
					required = "&nbsp;";
				
				item.add(new Label("required", required).setEscapeModelStrings(false));
				
				item.add((Component)propertyContext.renderForEdit("value"));
				
				String description = EditableUtils.getDescription(propertyContext.getPropertyGetter());
				if (description != null)
					item.add(new Label("description", description).setEscapeModelStrings(false));
				else
					item.add(new Label("description").setVisible(false));
				
				item.add(new ListView<ValidationError>("propertyValidationErrors", propertyContext.getValidationErrors(false)) {

					@Override
					protected void populateItem(ListItem<ValidationError> item) {
						ValidationError error = item.getModelObject();
						item.add(new Label("propertyValidationError", error.toString()));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(!propertyContext.getValidationErrors(false).isEmpty());
					}
					
				});
				
				Map<Serializable, EditContext> childContexts = propertyContext.getChildContexts();
				if ((childContexts == null || childContexts.isEmpty()) && !propertyContext.getValidationErrors(false).isEmpty())
					item.add(AttributeModifier.append("class", "has-error"));
			}

		});
		
	}

}
