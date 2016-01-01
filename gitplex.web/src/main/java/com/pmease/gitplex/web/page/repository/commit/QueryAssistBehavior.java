package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.antlr.codeassist.Highlight;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.antlr.codeassist.SurroundingAware;
import com.pmease.commons.antlr.grammar.LexerRuleRefElementSpec;
import com.pmease.commons.git.NameAndEmail;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.Constants;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends ANTLRAssistBehavior {

	private final IModel<Repository> repoModel;
	
	private static final String[] DATE_EXAMPLES = new String[]{
			"one hour ago", "2 hours ago", "3PM", "noon", "today", "yesterday", 
			"yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "1 month 2 days ago"}; 
	
	public QueryAssistBehavior(IModel<Repository> repoModel) {
		super(CommitQueryParser.class, "query");
		this.repoModel = repoModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		repoModel.detach();
	}

	@Override
	protected List<InputSuggestion> suggest(final ParentedElement element, String matchWith, final int count) {
		if (element.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) element.getSpec();
			if (spec.getRuleName().equals("Value")) {
				return new SurroundingAware(codeAssist.getGrammar(), "(", ")") {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						String lowerCaseMatchWith = matchWith.toLowerCase();
						int numSuggestions = 0;
						List<InputSuggestion> suggestions = new ArrayList<>();
						int tokenType = element.getRoot().getLastMatchedToken().getType();
						if (tokenType == CommitQueryParser.BRANCH) {
							for (String value: repoModel.getObject().getBranches()) {
								int index = value.toLowerCase().indexOf(lowerCaseMatchWith);
								if (index != -1 && numSuggestions++<count) {
									Highlight highlight = new Highlight(index, index+lowerCaseMatchWith.length());
									suggestions.add(new InputSuggestion(value, highlight));
								}
							}
						} else if (tokenType == CommitQueryParser.TAG) {
							for (String value: repoModel.getObject().getTags()) {
								int index = value.toLowerCase().indexOf(lowerCaseMatchWith);
								if (index != -1 && numSuggestions++<count) {
									Highlight highlight = new Highlight(index, index+lowerCaseMatchWith.length());
									suggestions.add(new InputSuggestion(value, highlight));
								}
							}
						} else if (tokenType == CommitQueryParser.AUTHOR 
								|| tokenType == CommitQueryParser.COMMITTER) {
							suggestions.add(InputSuggestion.hint("Use * to match any string"));
							Set<String> suggestedInputs = new LinkedHashSet<>();
							AuxiliaryManager auxiliaryManager = GitPlex.getInstance(AuxiliaryManager.class);
							List<NameAndEmail> contributors = auxiliaryManager.getContributors(repoModel.getObject());
							for (NameAndEmail contributor: contributors) {
								String content;
								if (StringUtils.isNotBlank(contributor.getEmailAddress()))
									content = contributor.getName() + " <" + contributor.getEmailAddress() + ">";
								else
									content = contributor.getName();
								String wildcarded = WildcardUtils.applyWildcard(content, lowerCaseMatchWith, false);
								if (wildcarded != null) {
									suggestedInputs.add(wildcarded);
									if (suggestedInputs.size() == count)
										break;
								}
							}
							
							for (String suggestedInput: suggestedInputs) { 
								int index = suggestedInput.toLowerCase().indexOf(lowerCaseMatchWith.toLowerCase());
								Highlight highlight = new Highlight(index, index+lowerCaseMatchWith.length());
								suggestions.add(new InputSuggestion(suggestedInput, -1, true, null, highlight));
							}
						} else if (tokenType == CommitQueryParser.BEFORE 
								|| tokenType == CommitQueryParser.AFTER) {
							if (!lowerCaseMatchWith.endsWith(")")) {
								suggestions.add(new InputSuggestion(Constants.DATETIME_FORMATTER.print(System.currentTimeMillis())));
								suggestions.add(new InputSuggestion(Constants.DATE_FORMATTER.print(System.currentTimeMillis())));
								for (String dateExample: DATE_EXAMPLES) 
									suggestions.add(new InputSuggestion(dateExample));
							}
						} else if (tokenType == CommitQueryParser.PATH) {
							suggestions.add(InputSuggestion.hint("Use * to match any string"));
							Set<String> suggestedInputs = new LinkedHashSet<>();
							AuxiliaryManager auxiliaryManager = GitPlex.getInstance(AuxiliaryManager.class);
							for (String path: auxiliaryManager.getFiles(repoModel.getObject())) {
								String wildcarded = WildcardUtils.applyWildcard(path, lowerCaseMatchWith, false);
								if (wildcarded != null) {
									int matchEnd = wildcarded.toLowerCase().indexOf(lowerCaseMatchWith) + lowerCaseMatchWith.length();
									String suffix = wildcarded.substring(matchEnd);
									int index = suffix.indexOf('/');
									String suggestedInput = wildcarded.substring(0, matchEnd);
									if (index != -1)
										suggestedInput += suffix.substring(0, index) + "/";
									else
										suggestedInput += suffix;
									suggestedInputs.add(suggestedInput);
									if (suggestedInputs.size() == count)
										break;
								}
							}
							
							for (String suggestedInput: suggestedInputs) { 
								int caret;
								if (suggestedInput.endsWith("/"))
									caret = suggestedInput.length();
								else
									caret = -1;
								int index = suggestedInput.toLowerCase().indexOf(lowerCaseMatchWith.toLowerCase());
								Highlight highlight = new Highlight(index, index+lowerCaseMatchWith.length());
								suggestions.add(new InputSuggestion(suggestedInput, caret, true, null, highlight));
								if (suggestions.size() == count)
									break;
							}
						}
						return suggestions;
					}
					
				}.suggest(element.getSpec(), matchWith);
			}
		} 
		return null;
	}
	
	@Override
	protected InputSuggestion wrapAsSuggestion(ParentedElement expectedElement, 
			String suggestedLiteral, boolean complete) {
		String description;
		switch (suggestedLiteral) {
		case "branch": 
			description = "commits of branch";
			break;
		case "tag":
			description = "commits of tag";
			break;
		case "id":
			description = "commits of hash";
			break;
		case "committer":
			description = "committed by";
			break;
		case "author":
			description = "authored by";
			break;
		case "message":
			description = "commit message contains";
			break;
		case "before":
			description = "before specified date";
			break;
		case "after":
			description = "after specified date";
			break;
		case "path":
			description = "touching specified path";
			break;
		case "^":
			description = "exclude";
			break;
		case "..":
		case "...":
			description = "range";
			break;
		default:
			description = null;
		}
		return new InputSuggestion(suggestedLiteral, description, null);
	}

}
