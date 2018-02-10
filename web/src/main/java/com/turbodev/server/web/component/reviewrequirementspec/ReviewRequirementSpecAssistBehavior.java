package com.turbodev.server.web.component.reviewrequirementspec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.turbodev.codeassist.FenceAware;
import com.turbodev.codeassist.InputSuggestion;
import com.turbodev.codeassist.ParentedElement;
import com.turbodev.codeassist.grammar.ElementSpec;
import com.turbodev.codeassist.grammar.LexerRuleRefElementSpec;
import com.turbodev.codeassist.grammar.RuleRefElementSpec;
import com.turbodev.server.util.reviewrequirement.ReviewRequirementSpecParser;
import com.turbodev.server.model.Project;
import com.turbodev.server.security.ProjectPrivilege;
import com.turbodev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import com.turbodev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class ReviewRequirementSpecAssistBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	public ReviewRequirementSpecAssistBehavior(IModel<Project> projectModel) {
		super(ReviewRequirementSpecParser.class, "spec");
		this.projectModel = projectModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		projectModel.detach();
	}

	@Override
	protected List<InputSuggestion> suggest(final ParentedElement expectedElement, String matchWith) {
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						if (expectedElement.findParentByRule("userCriteria") != null) {
							return SuggestionUtils.suggestUser(projectModel.getObject(), ProjectPrivilege.READ, 
									unfencedMatchWith);
						} else {
							return SuggestionUtils.suggestGroup(unfencedMatchWith);
						}
					}

					@Override
					protected String getFencingDescription() {
						return "value needs to be enclosed in brackets";
					}
					
				}.suggest(expectedElement.getSpec(), matchWith);
			} else if (spec.getRuleName().equals("DIGIT")) {
				List<InputSuggestion> suggestions = new ArrayList<>();
				suggestions.add(new InputSuggestion("1", "require one review from the group", null));
				suggestions.add(new InputSuggestion("2", "require two reviews from the group", null));
				suggestions.add(new InputSuggestion("3", "require three reviews from the group", null));
				
				for (Iterator<InputSuggestion> it = suggestions.iterator(); it.hasNext();) {
					InputSuggestion suggestion = it.next();
					if (!suggestion.getContent().startsWith(matchWith) || suggestion.getContent().equals(matchWith))
						it.remove();
				}
				
				return suggestions;
			}
		}
		return null;
	}
	
	@Override
	protected List<String> getHints(ParentedElement expectedElement, String matchWith) {
		List<String> hints = new ArrayList<>();
		if (expectedElement.getSpec() instanceof RuleRefElementSpec) {
			RuleRefElementSpec spec = (RuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("count")) {
				hints.add("Specify required reviews from the group");
				hints.add("If not specified, one review is required from the group");
			}
		} 
		return hints;
	}

	@Override
	protected InputSuggestion wrapAsSuggestion(ParentedElement expectedElement, String suggestedLiteral, 
			boolean complete) {
		String description;
		switch (suggestedLiteral) {
		case "all":
			description = "require reviews from all members of the group"; 
			break;
		case ":":
			description = "number of required reviews from the group";
			break;
		case " ":
			description = "space";
			break;
		default:
			description = null;
		}
		return new InputSuggestion(suggestedLiteral, description, null);
	}

	@Override
	protected int getEndOfMatch(ElementSpec spec, String content) {
		if (content.startsWith(VALUE_OPEN+VALUE_CLOSE))
			return 2;
		else
			return super.getEndOfMatch(spec, content);
	}

}
