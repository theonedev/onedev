package com.pmease.gitplex.web.page.test;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private static final String[] AUTHORS = new String[]{"\"robin shen\"", "\"steve luo\"", "justin"};
	
	private static final String[] STATUS = new String[]{"open", "close"};

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
//						QueryContext query = parser.query();
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
		input.add(new ANTLRAssistBehavior(PostQueryParser.class, "query") {
			
			@Override
			protected List<InputSuggestion> suggest(final ParseTree parseTree, Node elementNode, String matchWith) {
				if (elementNode.getSpec() instanceof RuleRefElementSpec) {
					RuleRefElementSpec spec = (RuleRefElementSpec) elementNode.getSpec();
					if (spec.getRuleName().equals("criteria")) {
						List<InputSuggestion> suggestions;
						if (matchWith.startsWith("author:") || matchWith.startsWith("-author:")) {
							suggestions = suggestAuthor(matchWith);
						} else if (matchWith.startsWith("is:") || matchWith.startsWith("-is:")) {
							suggestions = suggestStatus(matchWith);
						} else {
							suggestions = new ArrayList<>();
						}
						return suggestions;
					}
				}
				return null;
			}

		});
		form.add(input);
		form.add(new AjaxButton("query") {});
		add(form);
	}

	private List<InputSuggestion> suggestAuthor(String matchWith) {
		String key;
		String query;
		if (matchWith.startsWith("author:")) {
			key = "author:";
			query = matchWith.substring("author:".length());
		} else {
			key = "-author:";
			query = matchWith.substring("-author:".length());
		}
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (String author: AUTHORS) {
			if (StringUtils.deleteWhitespace(author).contains(query))
				suggestions.add(new InputSuggestion(key + author, author, null));
		}
		return suggestions;
	}
	
	private List<InputSuggestion> suggestStatus(String matchWith) {
		String key;
		String query;
		if (matchWith.startsWith("is:")) {
			key = "is:";
			query = matchWith.substring("is:".length());
		} else {
			key = "-is:";
			query = matchWith.substring("-is:".length());
		}
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (String status: STATUS) {
			if (StringUtils.deleteWhitespace(status).contains(query))
				suggestions.add(new InputSuggestion(key + status, status, null));
		}
		return suggestions;
	}
	
}
