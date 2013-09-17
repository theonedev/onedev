package com.pmease.commons.wicket.editable.reflection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

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
		
		add(new ListView<PropertyEditContext<RenderContext>>("properties", editContext.getPropertyContexts()) {

			@Override
			protected void populateItem(ListItem<PropertyEditContext<RenderContext>> item) {
				final PropertyEditContext<RenderContext> propertyContext = item.getModelObject();
				item.add(new Label("name", EditableUtils.getName(propertyContext.getPropertyGetter())));
				propertyContext.renderForEdit(new RenderContext(item, "value"));
				
				item.add(new ListView<ValidationError>("propertyValidationErrors", propertyContext.getValidationErrors()) {

					@Override
					protected void populateItem(ListItem<ValidationError> item) {
						ValidationError error = item.getModelObject();
						item.add(new Label("propertyValidationError", error.toString()));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(!propertyContext.getValidationErrors().isEmpty());
					}
					
				});
				
			}

		});
		
		add(new ListView<ValidationError>("beanValidationErrors", editContext.getValidationErrors()) {

			@Override
			protected void populateItem(ListItem<ValidationError> item) {
				ValidationError error = item.getModelObject();
				item.add(new Label("beanValidationError", error.toString()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!editContext.getValidationErrors().isEmpty());
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new EditableResourceReference()));
	}

}
