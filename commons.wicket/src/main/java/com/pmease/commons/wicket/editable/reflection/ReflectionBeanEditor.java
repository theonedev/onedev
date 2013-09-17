package com.pmease.commons.wicket.editable.reflection;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.editable.ValidationError;
import com.pmease.commons.wicket.editable.EditableResourceReference;
import com.pmease.commons.wicket.editable.RenderContext;

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

		add(new ListView<PropertyEditContext<RenderContext>>("properties", editContext.getPropertyContexts()) {

			@Override
			protected void populateItem(ListItem<PropertyEditContext<RenderContext>> item) {
				final PropertyEditContext<RenderContext> propertyContext = item.getModelObject();
				item.add(new Label("name", EditableUtils.getName(propertyContext.getPropertyGetter())));
				propertyContext.renderForEdit(new RenderContext(item, "value"));
				
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
				
				if (!propertyContext.getValidationErrors(false).isEmpty())
					item.add(AttributeModifier.append("class", "has-error"));
			}

		});
		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new EditableResourceReference()));
	}

}
