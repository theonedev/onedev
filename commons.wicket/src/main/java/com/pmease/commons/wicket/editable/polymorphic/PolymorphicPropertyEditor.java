package com.pmease.commons.wicket.editable.polymorphic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.wicket.editable.EditableHeaderItem;

@SuppressWarnings("serial")
public class PolymorphicPropertyEditor extends Panel {

	private final PolymorphicPropertyEditContext editContext;
	
	private static final String VALUE_EDITOR_ID = "valueEditor";
	
	public PolymorphicPropertyEditor(String id, PolymorphicPropertyEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<String> implementationNames = new ArrayList<String>();
		for (Class<?> each: editContext.getImplementations())
			implementationNames.add(EditableUtils.getName(each));
				
		add(new DropDownChoice<String>("typeSelector", new IModel<String>() {
			
			@Override
			public void detach() {
				
			}

			@Override
			public String getObject() {
				Serializable propertyValue = editContext.getPropertyValue();
				if (propertyValue != null)
					return EditableUtils.getName(propertyValue.getClass());
				else
					return null;
			}

			@Override
			public void setObject(String object) {
				boolean found = false;
				
				for (Class<?> each: editContext.getImplementations()) {
					if (EditableUtils.getName(each).equals(object)) {
						editContext.setPropertyValue(editContext.instantiate(each));
						found = true;
					}
				}
				
				if (!found)
					editContext.setPropertyValue(null);
			}
			
		}, implementationNames).setNullValid(!editContext.isPropertyRequired()).add(new AjaxFormComponentUpdatingBehavior("onclick"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				Component valueEditor = newValueEditor();
				replace(valueEditor);
				target.add(valueEditor);
			}
			
		}));

		add(newValueEditor());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(EditableHeaderItem.get());
	}

	private Component newValueEditor() {
		EditContext valueContext = editContext.getValueContext();
		Component valueEditor;
		if (valueContext != null) {
			valueEditor = (Component)valueContext.renderForEdit(VALUE_EDITOR_ID);
		} else {
			valueEditor = new WebMarkupContainer(VALUE_EDITOR_ID).setVisible(false);
		}
		valueEditor.setOutputMarkupId(true);
		valueEditor.setOutputMarkupPlaceholderTag(true);
		return valueEditor;
	}

}
