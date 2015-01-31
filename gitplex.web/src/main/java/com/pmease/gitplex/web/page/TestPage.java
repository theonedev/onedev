package com.pmease.gitplex.web.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.behavior.comment.CommentMarkdownBehavior;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private String markdown;
	
	public TestPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.add(new TextArea<String>("input", new PropertyModel<String>(this, "markdown")).add(new CommentMarkdownBehavior()));
		form.add(new AjaxSubmitLink("save", form) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				System.out.println(markdown);
			}
			
		});
		add(form);
	}

}
