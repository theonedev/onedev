package com.pmease.gitop.web.page.repository.settings.gatekeeper;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.wicket.editor.BeanEditContext;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

@SuppressWarnings("serial")
public abstract class GateKeeperEditor extends Panel {

	private GateKeeper gateKeeper;
	
	public GateKeeperEditor(String id, GateKeeper gateKeeper) {
		super(id);
		this.gateKeeper = SerializationUtils.clone(gateKeeper);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String iconClass = EditableUtils.getIcon(gateKeeper.getClass());
		if (iconClass == null)
			iconClass = "icon-lock";
		add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", iconClass)));
		add(new Label("name", EditableUtils.getName(gateKeeper.getClass())));
		add(new Label("description", EditableUtils.getDescription(gateKeeper.getClass())).setEscapeModelStrings(false));
		
		Form<?> form = new Form<Void>("form");
		add(form);
		form.add(BeanEditContext.edit("beanEditor", gateKeeper));
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		form.add(new AjaxSubmitLink("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSave(target, gateKeeper);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(GateKeeperEditor.this);
			}
			
		});
	}
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
	protected abstract void onSave(AjaxRequestTarget target, GateKeeper gateKeeper);
}
