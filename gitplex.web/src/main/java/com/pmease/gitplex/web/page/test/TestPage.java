package com.pmease.gitplex.web.page.test;

import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.antlr.codeassist.test.CodeAssistTest2Parser;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.web.page.base.BasePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.add(new NotificationPanel("feedback", form));
		
		form.add(new TextField<String>("input", Model.of("")).add(new ANTLRAssistBehavior(CodeAssistTest2Parser.class, "query") {

			@Override
			protected List<InputSuggestion> suggest(ParentedElement element, String matchWith, final int count) {
				return null;
			}
			
		}));
		add(form);
	}

}
