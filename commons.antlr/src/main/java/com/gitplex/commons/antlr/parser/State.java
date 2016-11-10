package com.gitplex.commons.antlr.parser;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.gitplex.commons.antlr.grammar.AlternativeSpec;
import com.gitplex.commons.antlr.grammar.ElementSpec;
import com.gitplex.commons.antlr.grammar.RuleSpec;

/**
 * Represents a Earley state as mentioned in https://en.wikipedia.org/wiki/Earley_parser
 * 
 * @author robin
 *
 */
public class State {
	
	private final int originPosition;
	
	private final RuleSpec ruleSpec;
	
	private final int alternativeSpecIndex;
	
	private final int expectedElementSpecIndex;
	
	private final boolean expectedElementSpecMatchedOnce;
	
	private final List<Element> elements;
	
	private transient AlternativeSpec alternativeSpec;
	
	private transient List<ElementSpec> elementSpecs;

	/**
	 * Construct a Earley state.
	 * 
	 * @param originPosition
	 *			position of the chart originating this state. Representing index of first token matched by the state 
	 * @param ruleSpec
	 * 			rule spec of the state
	 * @param alternativeSpecIndex
	 * 			index of alternative spec above rule 
	 * @param expectedElementSpecIndex
	 * 			index of element spec expecting to be matched, representing dot position 
	 * 			of the state
	 * @param expectedElementSpecMatchedOnce
	 * 			whether or not the expected element spec has been matched at least once to
	 * 			facilitate handling of element multiplicity (* and +)
	 * @param elements
	 * 			already matched elements in this state
	 */
	public State(int originPosition, RuleSpec ruleSpec, int alternativeSpecIndex, int expectedElementSpecIndex, 
			boolean expectedElementSpecMatchedOnce, List<Element> elements) {
		this.originPosition = originPosition;
		this.ruleSpec = ruleSpec;
		this.alternativeSpecIndex = alternativeSpecIndex;
		this.expectedElementSpecIndex = expectedElementSpecIndex;
		this.expectedElementSpecMatchedOnce = expectedElementSpecMatchedOnce;
		this.elements = elements;
	}
	
	/**
	 * Get origin position of this state
	 * 
	 * @return
	 * 			position of the chart originating this state. Representing index of first token matched by the state
	 */
	public int getOriginPosition() {
		return originPosition;
	}

	public RuleSpec getRuleSpec() {
		return ruleSpec;
	}

	public int getAlternativeSpecIndex() {
		return alternativeSpecIndex;
	}

	public int getExpectedElementSpecIndex() {
		return expectedElementSpecIndex;
	}

	public boolean isExpectedElementSpecMatchedOnce() {
		return expectedElementSpecMatchedOnce;
	}
	
	/**
	 * Get expected element spec
	 * 
	 * @return
	 * 			element spec expected to be matched next, or <tt>null</tt> if 
	 * 			the state is completed
	 */
	@Nullable
	public ElementSpec getExpectedElementSpec() {
		if (isCompleted())
			return null;
		else
			return getElementSpecs().get(expectedElementSpecIndex);
	}

	/**
	 * Get elements already matched in this state
	 * 
	 * @return
	 * 			elements already matched in this state
	 */
	public List<Element> getElements() {
		return elements;
	}
	
	public AlternativeSpec getAlternativeSpec() {
		if (alternativeSpec == null)
			alternativeSpec = ruleSpec.getAlternatives().get(alternativeSpecIndex);
		return alternativeSpec;
	}
	
	public List<ElementSpec> getElementSpecs() {
		if (elementSpecs == null)
			elementSpecs = getAlternativeSpec().getElements();
		return elementSpecs;
	}
	
	public boolean isCompleted() {
		return expectedElementSpecIndex == getElementSpecs().size();
	}

	public State getScannedState(EarleyParser parser, int tokenIndex) {
		ElementSpec expectedElementSpec = getExpectedElementSpec();
		List<Element> elements = new ArrayList<>(getElements());
		elements.add(new Element(parser, expectedElementSpec, tokenIndex+1, null));
		if (!expectedElementSpec.isMultiple()) {
			return new State(getOriginPosition(), getRuleSpec(), 
					getAlternativeSpecIndex(), getExpectedElementSpecIndex()+1, 
					false, elements);
		} else {
			return new State(getOriginPosition(), getRuleSpec(), 
					getAlternativeSpecIndex(), getExpectedElementSpecIndex(), 
					true, elements);
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof State))
			return false;
		if (this == other)
			return true;
		State otherState = (State) other;
		
		/*
		 * The standard Earley parser should also consider parsed elements into 
		 * account when compare state, however it will cause state to increase 
		 * quickly in case of ambiguity rules. By excluding parsed elements from 
		 * comparison, we normally get only the first possibility amongst all 
		 * the ambiguity possibilities, which is totally acceptable for our 
		 * code assistance purpose  
		 */
		return new EqualsBuilder()
				.append(originPosition, otherState.originPosition)
				.append(ruleSpec.getName(), otherState.ruleSpec.getName())
				.append(alternativeSpecIndex, otherState.alternativeSpecIndex)
				.append(expectedElementSpecIndex, otherState.expectedElementSpecIndex)
				.append(expectedElementSpecMatchedOnce, otherState.expectedElementSpecMatchedOnce)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(originPosition)
				.append(ruleSpec.getName())
				.append(alternativeSpecIndex)
				.append(expectedElementSpecIndex)
				.append(expectedElementSpecMatchedOnce)
				.toHashCode();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<expectedElementSpecIndex; i++)
			buffer.append(getElementSpecs().get(i)).append(" ");
		buffer.append(expectedElementSpecMatchedOnce?"~ ":"^ ");
		for (int i=expectedElementSpecIndex; i<getElementSpecs().size(); i++)
			buffer.append(getElementSpecs().get(i)).append(" ");
		
		return ruleSpec.getName() + " -> " + buffer.toString() + ": " + originPosition;
	}
	
}
