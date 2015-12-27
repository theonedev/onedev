package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.antlr.codeassist.SurroundingAware;
import com.pmease.commons.antlr.grammar.LexerRuleRefElementSpec;
import com.pmease.commons.git.NameAndEmail;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends ANTLRAssistBehavior {

	private final IModel<Repository> repoModel;
	
	private static final int MAX_SUGGESTIONS = 64;
	
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
	protected List<InputSuggestion> suggest(final ParentedElement element, String matchWith) {
		if (element.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) element.getSpec();
			if (spec.getRuleName().equals("Value")) {
				return new SurroundingAware(codeAssist.getGrammar(), "(", ")") {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						matchWith = matchWith.toLowerCase();
						int count = 0;
						List<InputSuggestion> suggestions = new ArrayList<>();
						int tokenType = element.getRoot().getLastMatchedToken().getType();
						if (tokenType == CommitQueryParser.BRANCH) {
							for (String value: repoModel.getObject().getBranches()) {
								if (value.toLowerCase().contains(matchWith) && count++<MAX_SUGGESTIONS)
									suggestions.add(new InputSuggestion(value));
							}
						} else if (tokenType == CommitQueryParser.TAG) {
							for (String value: repoModel.getObject().getTags()) {
								if (value.toLowerCase().contains(matchWith) && count++<MAX_SUGGESTIONS)
									suggestions.add(new InputSuggestion(value));
							}
						} else if (tokenType == CommitQueryParser.AUTHOR 
								|| tokenType == CommitQueryParser.COMMITTER) {
							AuxiliaryManager auxiliaryManager = GitPlex.getInstance(AuxiliaryManager.class);
							List<NameAndEmail> contributors = new ArrayList<>(auxiliaryManager
									.getContributors(repoModel.getObject()));
							Collections.sort(contributors);
							for (NameAndEmail contributor: contributors) {
								if ((contributor.getName().toLowerCase().contains(matchWith) || contributor.getEmailAddress().toLowerCase().contains(matchWith)) 
										&& count++<MAX_SUGGESTIONS) {
									String content;
									if (StringUtils.isNotBlank(contributor.getEmailAddress()))
										content = contributor.getName() + " <" + contributor.getEmailAddress() + ">";
									else
										content = contributor.getName();
									suggestions.add(new InputSuggestion(content.trim()));
								}
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
		return new InputSuggestion(suggestedLiteral, description);
	}

}
