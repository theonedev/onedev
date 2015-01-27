package com.pmease.gitplex.web.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.MarkdownManager;
import com.pmease.gitplex.web.component.markdown.MarkdownInput;

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
		form.add(new MarkdownInput("input", new PropertyModel<String>(this, "markdown")));
		form.add(new AjaxSubmitLink("save", form) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				System.out.println(markdown);
			}
			
		});
		add(form);
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				String markdown = "hello @admin ";
				String html = GitPlex.getInstance(MarkdownManager.class).toHtml(markdown, true, true);
				System.out.println(html);
			}
			
		});
	}

}
