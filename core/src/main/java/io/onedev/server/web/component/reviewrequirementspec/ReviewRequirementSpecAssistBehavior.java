package io.onedev.server.web.component.reviewrequirementspec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.google.common.base.Optional;

import io.onedev.codeassist.FenceAware;
import io.onedev.codeassist.InputSuggestion;
import io.onedev.codeassist.ParentedElement;
import io.onedev.codeassist.grammar.ElementSpec;
import io.onedev.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.codeassist.grammar.RuleRefElementSpec;
import io.onedev.server.model.Project;
import io.onedev.server.security.ProjectPrivilege;
import io.onedev.server.util.reviewrequirement.ReviewRequirementSpecParser;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class ReviewRequirementSpecAssistBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	private static final String ESCAPE_CHARS = "\\()";
	
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
									unfencedMatchWith, ESCAPE_CHARS);
						} else {
							return SuggestionUtils.suggestGroup(unfencedMatchWith, ESCAPE_CHARS);
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
	protected Optional<String> describe(ParentedElement expectedElement, String suggestedLiteral) {
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
		return Optional.fromNullable(description);
	}

	@Override
	protected int getEndOfMatch(ElementSpec spec, String content) {
		if (content.startsWith(VALUE_OPEN+VALUE_CLOSE))
			return 2;
		else
			return super.getEndOfMatch(spec, content);
	}

}
