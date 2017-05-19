package com.gitplex.server.web.component.reviewappointment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.gitplex.codeassist.FenceAware;
import com.gitplex.codeassist.InputSuggestion;
import com.gitplex.codeassist.ParentedElement;
import com.gitplex.codeassist.grammar.ElementSpec;
import com.gitplex.codeassist.grammar.LexerRuleRefElementSpec;
import com.gitplex.codeassist.grammar.RuleRefElementSpec;
import com.gitplex.server.model.Depot;
import com.gitplex.server.security.privilege.DepotPrivilege;
import com.gitplex.server.util.reviewappointment.ReviewAppointmentParser;
import com.gitplex.server.web.behavior.inputassist.ANTLRAssistBehavior;
import com.gitplex.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class ReviewAppointmentAssistBehavior extends ANTLRAssistBehavior {

	private final IModel<Depot> depotModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	public ReviewAppointmentAssistBehavior(IModel<Depot> depotModel) {
		super(ReviewAppointmentParser.class, "expr");
		this.depotModel = depotModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		depotModel.detach();
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
							return SuggestionUtils.suggestUser(depotModel.getObject(), DepotPrivilege.READ, 
									unfencedMatchWith);
						} else {
							return SuggestionUtils.suggestTeam(depotModel.getObject(), unfencedMatchWith);
						}
					}

					@Override
					protected String getFencingDescription() {
						return "value needs to be enclosed in brackets";
					}
					
				}.suggest(expectedElement.getSpec(), matchWith);
			} else if (spec.getRuleName().equals("DIGIT")) {
				List<InputSuggestion> suggestions = new ArrayList<>();
				suggestions.add(new InputSuggestion("1", "require one review from the team", null));
				suggestions.add(new InputSuggestion("2", "require two reviews from the team", null));
				suggestions.add(new InputSuggestion("3", "require three reviews from the team", null));
				
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
				hints.add("Specify required reviews from the team");
				hints.add("If not specified, one review is required from the team");
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
			description = "require reviews from all members of the team"; 
			break;
		case ":":
			description = "number of required reviews from the team";
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
