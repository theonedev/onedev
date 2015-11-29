package com.pmease.gitplex.web.page.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.SurroundingAware;
import com.pmease.commons.antlr.codeassist.TokenNode;
import com.pmease.commons.antlr.codeassist.test.CodeAssistTest3Parser;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private static final String[] AUTHORS = new String[]{"robin shen", "steve luo", "justin"};
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.add(new TextField<String>("input", Model.of("")).add(
				new ANTLRAssistBehavior(CodeAssistTest3Parser.class, "query") {
			
			@Override
			protected List<InputSuggestion> suggest(final ParseTree parseTree, Node elementNode, String matchWith) {
				if (elementNode.getSpec() instanceof RuleRefElementSpec) {
					RuleRefElementSpec spec = (RuleRefElementSpec) elementNode.getSpec();
					if (spec.getRuleName().equals("value")) {
						return new SurroundingAware(codeAssist, "\"", "\"") {

							@Override
							protected List<InputSuggestion> match(String matchWith) {
								List<InputSuggestion> suggestions = new ArrayList<>();
								Node criteria = parseTree.findParentNodeByRuleName(parseTree.getLastNode(), "criteria");
								TokenNode tokenNode = parseTree.getFirstTokenNode(criteria);
								if (tokenNode.getToken().getText().equals("author")) {
									for (String value: AUTHORS) {
										if (value.toLowerCase().contains(matchWith.toLowerCase()))
											suggestions.add(new InputSuggestion(value));
									}
								}
								return suggestions;
							}
							
						}.suggest(elementNode, matchWith);
					}
				}
				return null;
			}

			
		}));
		add(form);
	}

}
