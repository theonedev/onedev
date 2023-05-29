package io.onedev.server.web.component.user.accesstoken;

import io.onedev.server.model.support.AccessToken;
import io.onedev.server.web.editable.BeanContext;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
abstract class AccessTokenEditPanel extends Panel {

	private final AccessToken accessToken;
	
	public AccessTokenEditPanel(String id, AccessToken accessToken) {
		super(id);
		this.accessToken = accessToken;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		
		form.add(new Label("value", accessToken.getMaskedValue()));
		form.add(BeanContext.edit("editor", accessToken));
			
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSave(target, accessToken);
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
		
		setOutputMarkupId(true);
	}

	protected abstract void onSave(AjaxRequestTarget target, AccessToken accessToken);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
