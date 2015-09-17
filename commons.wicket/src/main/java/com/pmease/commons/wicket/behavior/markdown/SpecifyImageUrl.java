package com.pmease.commons.wicket.behavior.markdown;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.feedback.FeedbackPanel;

@SuppressWarnings("serial")
abstract class SpecifyImageUrl extends Panel implements ImageUrlProvider {

	private String url = "http://";
	
	public SpecifyImageUrl(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
			}
			
		};
		form.add(new FeedbackPanel("feedback", form));
		form.add(new TextField<String>("url", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return url;
			}

			@Override
			public void setObject(String object) {
				url = object;
			}
			
		}));
		form.add(new AjaxButton("insert") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (StringUtils.isBlank(url)) {
					form.error("Url should not be empty");
					target.add(form);
				} else if (!url.startsWith("http://") && !url.startsWith("https://")) {
					form.error("Url should start with http:// or https://");
					target.add(form);
				} else {
					onInsert(target, url);
				}
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		add(form);
	}

}
