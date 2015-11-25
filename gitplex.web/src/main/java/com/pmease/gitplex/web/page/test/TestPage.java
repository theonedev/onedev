package com.pmease.gitplex.web.page.test;

import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.lang.extractors.java.JavaParser;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.add(new TextArea<String>("input", Model.of("")).add(new ANTLRAssistBehavior(JavaParser.class, "compilationUnit") {
			
			@Override
			protected List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith) {
				return null;
			}

			
		}));
		add(form);
	}

}
