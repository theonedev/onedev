package com.pmease.commons.wicket.component;

import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;

public class SubmitTypeAjaxSubmitLink extends AjaxSubmitLink {

	private static final long serialVersionUID = 1L;

	public SubmitTypeAjaxSubmitLink(String id) {
		super(id);
	}

	public SubmitTypeAjaxSubmitLink(String id, Form<?> form) {
		super(id, form);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.getAttributes().put("type", "submit");
	}
	
}
