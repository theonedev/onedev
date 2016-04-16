package com.pmease.commons.wicket.component;

import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

public class SubmitTypeAjaxButton extends AjaxButton {

	private static final long serialVersionUID = 1L;

	public SubmitTypeAjaxButton(String id) {
		super(id);
	}

	public SubmitTypeAjaxButton(String id, IModel<String> model) {
		super(id, model);
	}

	public SubmitTypeAjaxButton(String id, Form<?> form) {
		super(id, form);
	}

	public SubmitTypeAjaxButton(String id, IModel<String> model, final Form<?> form) {
		super(id, model, form);
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.getAttributes().put("type", "submit");
	}
	
}
