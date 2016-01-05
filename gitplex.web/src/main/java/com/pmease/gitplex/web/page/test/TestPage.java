package com.pmease.gitplex.web.page.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;

import com.pmease.commons.antlr.codeassist.InputCompletion;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.util.Range;
import com.pmease.commons.wicket.behavior.inputassist.InputAssistBehavior;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.add(new TextField<Void>("input").add(new InputAssistBehavior() {

			@Override
			protected List<InputCompletion> getSuggestions(InputStatus inputStatus, int count) {
				List<InputCompletion> suggestions = new ArrayList<>();
				suggestions.add(new InputCompletion(0, 0, "hello world just do it well we can handle it", 0, "hello world just do it well we c", null));
				return suggestions;
			}

			@Override
			protected List<Range> getErrors(String inputContent) {
				return new ArrayList<>();
			}

			@Override
			protected int getAnchor(String content) {
				return 0;
			}
			
		}));
		add(form);
	}

}
