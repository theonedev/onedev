package com.pmease.commons.antlr.codeassist;

import java.util.List;

public class RuleRefElementSpec extends ElementSpec {

	private final String ruleName;
	
	private transient RuleSpec rule;
	
	public RuleRefElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, String ruleName) {
		super(codeAssist, label, multiplicity);
		
		this.ruleName = ruleName;
	}

	@Override
	protected boolean matchEmptyInElement() {
		return getRule().matchEmpty();
	}

	public RuleSpec getRule() {
		if (rule == null)
			rule = codeAssist.getRule(ruleName);
		return rule;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith) {
		return getRule().suggestFirst(new Node(this, parent), matchWith);
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
