package io.onedev.server.web.component.beaneditmodal;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;

@SuppressWarnings("serial")
abstract class BeanEditContentPanel extends Panel {

	public BeanEditContentPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		form.setOutputMarkupId(true);
		
		if (getTitle() != null)
			form.add(new Label("title", getTitle()));
		else
			form.add(new Label("title", EditableUtils.getDisplayName(getBean().getClass())));
		
		form.add(BeanContext.edit("editor", getBean(), getPropertyNames(), isExclude()));
		
		form.add(new AjaxButton("ok") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSave(target, getBean());
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				BeanEditContentPanel.this.onCancel(target);
			}
			
		});
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				BeanEditContentPanel.this.onCancel(target);
			}
			
		});
		add(form);
	}

	protected abstract Serializable getBean();
	
	protected abstract Collection<String> getPropertyNames();
	
	protected abstract boolean isExclude(); 
	
	@Nullable
	protected abstract String getTitle();
	
	protected abstract void onSave(AjaxRequestTarget target, Serializable bean);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
