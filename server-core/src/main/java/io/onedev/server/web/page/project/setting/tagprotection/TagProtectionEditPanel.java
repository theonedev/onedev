package io.onedev.server.web.page.project.setting.tagprotection;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.model.support.TagProtection;
import io.onedev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
abstract class TagProtectionEditPanel extends Panel {

	private final TagProtection protection;
	
	public TagProtectionEditPanel(String id, TagProtection protection) {
		super(id);
		
		this.protection = protection;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		Form<?> form = new Form<Void>("form");
		form.add(new FencedFeedbackPanel("feedback", form));

		form.add(BeanContext.edit("editor", protection));
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSave(target, protection);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		add(form);
	}

	protected abstract void onSave(AjaxRequestTarget target, TagProtection protection);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
