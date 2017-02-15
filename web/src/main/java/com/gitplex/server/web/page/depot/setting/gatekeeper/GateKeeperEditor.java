package com.gitplex.server.web.page.depot.setting.gatekeeper;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.server.gatekeeper.GateKeeper;
import com.gitplex.server.util.editable.EditableUtils;
import com.gitplex.server.web.editable.BeanContext;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class GateKeeperEditor extends Panel {

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
			iconClass = "fa-lock";
		add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", iconClass)));
		add(new Label("name", EditableUtils.getName(gateKeeper.getClass())));
		add(new Label("description", EditableUtils.getDescription(gateKeeper.getClass())).setEscapeModelStrings(false));
		
		Form<?> form = new Form<Void>("form");
		add(form);
		form.add(new NotificationPanel("feedback", form));
		form.add(BeanContext.editBean("beanEditor", gateKeeper));
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSave(target, gateKeeper);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.getAttributes().put("type", "submit");
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(GateKeeperEditor.this);
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
	protected abstract void onSave(AjaxRequestTarget target, GateKeeper gateKeeper);
}
