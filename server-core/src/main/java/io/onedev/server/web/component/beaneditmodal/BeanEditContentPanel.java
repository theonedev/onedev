package io.onedev.server.web.component.beaneditmodal;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.EditableUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import org.jspecify.annotations.Nullable;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.Collection;

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
			form.add(new Label("title", _T(EditableUtils.getDisplayName(getBean().getClass()))));
		
		String description = StringUtils.trimToNull(StringUtils.stripStart(
				EditableUtils.getDescription(getBean().getClass()), "."));
		if (description != null)
			form.add(new Label("description", description).setEscapeModelStrings(false));
		else
			form.add(new WebMarkupContainer("description").setVisible(false));
		
		form.add(new FencedFeedbackPanel("feedback", form));
		
		BeanEditor editor = BeanContext.edit("editor", getBean(), getPropertyNames(), isExclude());
		form.add(editor);
		
		form.add(new AjaxButton("ok") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (isDirtyAware())
					add(AttributeAppender.append("class", "dirty-aware"));
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				var errorMessage = onSave(target);
				if (errorMessage != null) {
					form.error(errorMessage);
					target.add(BeanEditContentPanel.this);
				}
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(BeanEditContentPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				BeanEditContentPanel.this.onCancel(target);
			}
			
		});
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(BeanEditContentPanel.this));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				BeanEditContentPanel.this.onCancel(target);
			}
			
		});
		add(form);
		
		setOutputMarkupId(true);
	}

	protected abstract Serializable getBean();
	
	protected abstract Collection<String> getPropertyNames();
	
	protected abstract boolean isExclude(); 
	
	@Nullable
	protected abstract String getTitle();
	
	protected abstract String onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
	protected boolean isDirtyAware() {
		return true;
	}
	
}
