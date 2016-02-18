package com.pmease.gitplex.web.component.refmatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Ref;

import com.pmease.commons.antlr.codeassist.FenceAware;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.LexerRuleRefElementSpec;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.Range;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.util.refmatch.RefMatchParser;

@SuppressWarnings("serial")
public class RefMatchBehavior extends ANTLRAssistBehavior {

	private final IModel<Depot> depotModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	public RefMatchBehavior(IModel<Depot> depotModel) {
		super(RefMatchParser.class, "match");
		this.depotModel = depotModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		depotModel.detach();
	}

	@Override
	protected List<InputSuggestion> suggest(final ParentedElement expectedElement, String matchWith, final int count) {
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						int tokenType = expectedElement.getRoot().getLastMatchedToken().getType();
						String unfencedLowerCaseMatchWith = unfencedMatchWith.toLowerCase();
						int numSuggestions = 0;
						List<InputSuggestion> suggestions = new ArrayList<>();
						switch (tokenType) {
						case RefMatchParser.BRANCH:
						case RefMatchParser.EXCLUDE_BRANCH:
							for (Ref ref: depotModel.getObject().getBranchRefs()) {
								String branch = GitUtils.ref2branch(ref.getName());
								int index = branch.toLowerCase().indexOf(unfencedLowerCaseMatchWith);
								if (index != -1 && numSuggestions++<count) {
									Range matchRange = new Range(index, index+unfencedLowerCaseMatchWith.length());
									suggestions.add(new InputSuggestion(branch, matchRange));
								}
							}
							break;
						case RefMatchParser.TAG:
						case RefMatchParser.EXCLUDE_TAG:
							for (Ref ref: depotModel.getObject().getTagRefs()) {
								String tag = GitUtils.ref2tag(ref.getName()); 
								int index = tag.toLowerCase().indexOf(unfencedLowerCaseMatchWith);
								if (index != -1 && numSuggestions++<count) {
									Range matchRange = new Range(index, index+unfencedLowerCaseMatchWith.length());
									suggestions.add(new InputSuggestion(tag, matchRange));
								}
							}
							break;
						case RefMatchParser.PATTERN:
						case RefMatchParser.EXCLUDE_PATTERN:
							if (!unfencedMatchWith.contains(VALUE_CLOSE)) {
								if (unfencedMatchWith.length() != 0) {
									String fenced = VALUE_OPEN + unfencedMatchWith + VALUE_CLOSE; 
									Range matchRange = new Range(0, fenced.length());
									suggestions.add(new InputSuggestion(fenced, -1, true, getFencingDescription(), matchRange));
								}
								suggestions.add(new InputSuggestion("refs/heads/**", "All branches", null));
								suggestions.add(new InputSuggestion("refs/tags/**", "All tags", null));
							}
							break;
						} 
						return suggestions;
					}

					@Override
					protected String getFencingDescription() {
						return "value needs to be enclosed in brackets";
					}
					
				}.suggest(expectedElement.getSpec(), matchWith);
			}
		} 
		return null;
	}
	
	@Override
	protected List<String> getHints(ParentedElement expectedElement, String matchWith) {
		List<String> hints = new ArrayList<>();
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value") && !matchWith.contains(VALUE_CLOSE)) {
				int tokenType = expectedElement.getRoot().getLastMatchedToken().getType();
				if (tokenType == RefMatchParser.PATTERN || tokenType == RefMatchParser.EXCLUDE_PATTERN) {
					hints.add("Use * to match any character except /");
					hints.add("Use ** to match any character including /");
				}
			}
		} 
		return hints;
	}

	@Override
	protected int getEndOfMatch(ElementSpec spec, String content) {
		if (content.startsWith(VALUE_OPEN+VALUE_CLOSE))
			return 2;
		else
			return super.getEndOfMatch(spec, content);
	}

	@Override
	protected InputSuggestion wrapAsSuggestion(ParentedElement expectedElement, 
			String suggestedLiteral, boolean complete) {
		String description;
		switch (suggestedLiteral) {
		case "branch": 
			description = "specify branch";
			break;
		case "tag":
			description = "specify tag";
			break;
		case "pattern":
			description = "specify ref pattern";
			break;
		case "excludeBranch":
			description = "exclude branch";
			break;
		case "excludeTag":
			description = "exclude tag";
			break;
		case "excludePattern":
			description = "exclude ref pattern";
			break;
		default:
			description = null;
		}
		return new InputSuggestion(suggestedLiteral, description, null);
	}

}
