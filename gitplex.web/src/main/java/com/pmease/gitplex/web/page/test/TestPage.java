package com.pmease.gitplex.web.page.test;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;

import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final FileUploadField uploadField = new FileUploadField("file");

		Form<Void> form = new Form<Void>("form");
		form.add(uploadField);
		
		form.add(new AjaxSubmitLink("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				try {
					uploadField.getFileUpload().writeTo(new File("w:\\temp\\a.jpg"));
				} catch (IOException e) {
				}
			}
			
		});
		add(form);
	}		

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}
	
}
