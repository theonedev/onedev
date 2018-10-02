package io.onedev.server.web.component.review;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.google.common.base.Optional;

import io.onedev.codeassist.FenceAware;
import io.onedev.codeassist.InputSuggestion;
import io.onedev.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.codeassist.grammar.RuleRefElementSpec;
import io.onedev.codeassist.parser.ParseExpect;
import io.onedev.codeassist.parser.TerminalExpect;
import io.onedev.server.model.Project;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.reviewrequirement.ReviewRequirementParser;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class ReviewRequirementAssistBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	private static final String ESCAPE_CHARS = "\\()";
	
	public ReviewRequirementAssistBehavior(IModel<Project> projectModel) {
		super(ReviewRequirementParser.class, "requirement", false);
		this.projectModel = projectModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		projectModel.detach();
	}

	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						Project project = projectModel.getObject();
						if (terminalExpect.findExpectByRule("userCriteria") != null) {
							return SuggestionUtils.suggestUser(project, ProjectPrivilege.CODE_READ, 
									unfencedMatchWith, ESCAPE_CHARS);
						} else {
							return SuggestionUtils.suggestTeam(project, unfencedMatchWith, ESCAPE_CHARS);
						}
					}

					@Override
					protected String getFencingDescription() {
						return "value needs to be enclosed in brackets";
					}
					
				}.suggest(terminalExpect);
			} else if (spec.getRuleName().equals("DIGIT")) {
				List<InputSuggestion> suggestions = new ArrayList<>();
				suggestions.add(new InputSuggestion("1", "require one review from the group", null));
				suggestions.add(new InputSuggestion("2", "require two reviews from the group", null));
				suggestions.add(new InputSuggestion("3", "require three reviews from the group", null));
				
				String matchWith = terminalExpect.getUnmatchedText();
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
	protected List<String> getHints(TerminalExpect terminalExpect) {
		List<String> hints = new ArrayList<>();
		if (terminalExpect.getParent() != null && terminalExpect.getParent().getElementSpec() instanceof RuleRefElementSpec) {
			RuleRefElementSpec spec = (RuleRefElementSpec) terminalExpect.getParent().getElementSpec();
			if (spec.getRuleName().equals("count")) {
				hints.add("Specify required reviews from the group");
				hints.add("If not specified, one review is required from the group");
			}
		} 
		return hints;
	}

	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
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

}
