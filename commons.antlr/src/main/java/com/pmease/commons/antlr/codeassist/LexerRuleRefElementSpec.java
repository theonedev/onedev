package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class LexerRuleRefElementSpec extends TokenElementSpec {

	private final String ruleName;
	
	private transient RuleSpec rule;
	
	public LexerRuleRefElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, 
			int tokenType, String ruleName) {
		super(codeAssist, label, multiplicity, tokenType);
		
		this.ruleName = ruleName;
	}

	public RuleSpec getRule() {
		if (rule == null)
			rule = codeAssist.getRule(ruleName);
		return rule;
	}

	@Override
	protected boolean matchEmptyInElement() {
		return getRule().matchEmpty();
	}

	@Override
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith) {
		return Lists.newArrayList(new ElementSuggestion(new Node(this, parent), new ArrayList<CaretAwareText>()));
	}

	@Override
	public CaretMove moveCaretToEdit(TokenStream stream) {
		if (getRule().getAlternatives().size() > 1) {
			return new CaretMove(0, true);
		} else {
			int offset = 0;
			AlternativeSpec alternativeSpec = getRule().getAlternatives().get(0);
			for (ElementSpec elementSpec: alternativeSpec.getElements()) {
				if (elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_ONE 
						|| elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_MORE) {
					return new CaretMove(offset, true);
				} else if (elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
					CaretMove move = elementSpec.moveCaretToEdit(stream);
					offset += move.getOffset();
					return new CaretMove(offset, true);
				} else {
					CaretMove move = elementSpec.moveCaretToEdit(stream);
					offset += move.getOffset();
					if (move.isStop())
						return new CaretMove(offset, true);
				}
			}
			return new CaretMove(offset, false);
		}
	}	
}
