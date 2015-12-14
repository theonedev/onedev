package com.pmease.gitplex.web.page.test;

import java.util.List;

import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.lang.extractors.java.JavaParser;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private String queryString;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
			}
			
		};
		TextArea<String> input = new TextArea<String>("input", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return queryString;
			}

			@Override
			public void setObject(String object) {
				queryString = object;
			}
			
		});
		input.add(new ANTLRAssistBehavior(JavaParser.class, "compilationUnit") {
			
			@Override
			protected List<InputSuggestion> suggest(ParentedElement element, String matchWith) {
				return null;
			}
			
		});
		form.add(input);
		form.add(new AjaxButton("query") {});
		add(form);
	}

}
