package com.pmease.gitplex.web.page.test;

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

		Form<Void> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				System.out.println(uploadField.getFileUpload().getClientFileName());
			}
			
		};
		form.add(uploadField);
		add(form);
	}		

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}
	
}
