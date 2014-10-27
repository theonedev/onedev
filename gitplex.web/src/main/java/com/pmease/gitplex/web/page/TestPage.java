package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private String name;
	
	public TestPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Form<Void>("form") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new TextField<String>("name", new PropertyModel<String>(TestPage.this, "name")));
				
				add(new SubmitLink("save") {

					@Override
					public void onSubmit() {
						super.onSubmit();
						System.out.println(name);
					}
					
				});
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();
				System.out.println(name);
			}
			
		});
	}

}
