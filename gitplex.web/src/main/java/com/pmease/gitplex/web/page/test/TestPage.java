package com.pmease.gitplex.web.page.test;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Lists;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.LiteralElementSpec;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.SurroundingAware;
import com.pmease.commons.antlr.codeassist.TokenNode;
import com.pmease.commons.antlr.codeassist.test.CodeAssistTest3Parser;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.web.page.base.BasePage;
import com.pmease.gitplex.web.page.test.PostQueryParser.CriteriaContext;
import com.pmease.gitplex.web.page.test.PostQueryParser.QueryContext;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private static final String[] AUTHORS = new String[]{"robin shen", "steve luo", "justin"};

	private String queryString;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				System.out.println("************************************");
				if (queryString != null) {
					try {
						ANTLRInputStream is = new ANTLRInputStream(queryString); 
						PostQueryLexer lexer = new PostQueryLexer(is);
						lexer.removeErrorListeners();
						CommonTokenStream tokens = new CommonTokenStream(lexer);
						PostQueryParser parser = new PostQueryParser(tokens);
						parser.removeErrorListeners();
						parser.setErrorHandler(new BailErrorStrategy());
						QueryContext query = parser.query();
						for (CriteriaContext criteria: query.criteria()) {
							System.out.println(criteria.key.getText() + ":" + criteria.value().getText());
						}
					} catch (Exception e) {
						System.out.println("query syntax error");
					}
				} else {
					System.out.println("empty query");
				}
			}
			
		};
		TextField<String> input = new TextField<String>("input", new IModel<String>() {

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
		input.add(new ANTLRAssistBehavior(CodeAssistTest3Parser.class, "query") {
			
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

							@Override
							protected String getSurroundingDescription() {
								return "value containing space has to be quoted";
							}
							
						}.suggest(elementNode, matchWith);
					}
				} else if (elementNode.getSpec() instanceof LiteralElementSpec) {
					LiteralElementSpec spec = (LiteralElementSpec) elementNode.getSpec();
					if (spec.getLiteral().toLowerCase().startsWith(matchWith.toLowerCase())) {
						String description;
						switch (spec.getLiteral()) {
						case "title": 
							description = "filter by title";
							break;
						case "author":
							description = "filter by author";
							break;
						default:
							description = null;
						}
						return Lists.newArrayList(new InputSuggestion(spec.getLiteral(), description));
					}
				} 
				return null;
			}

		});
		form.add(input);
		form.add(new AjaxButton("query") {});
		add(form);
	}

}
