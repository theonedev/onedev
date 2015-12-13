package com.pmease.commons.antlr.grammar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.antlr.codeassist.MandatoryScan;
import com.pmease.commons.antlr.grammar.ElementSpec.Multiplicity;

public class RuleSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final List<AlternativeSpec> alternatives;
	
	public RuleSpec(String name, List<AlternativeSpec> alternatives) {
		this.name = name;
		this.alternatives = alternatives;
	}
	
	public String getName() {
		return name;
	}

	public List<AlternativeSpec> getAlternatives() {
		return alternatives;
	}

	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		if (!checkedRules.contains(name)) { // to avoid infinite loop
			checkedRules.add(name);
		
			List<AlternativeSpec> alternatives = getAlternatives();
			// nothing will be mandatory if we have multiple alternatives 
			if (alternatives.size() == 1) {
				List<String> mandatories = new ArrayList<>();
				for (ElementSpec elementSpec: alternatives.get(0).getElements()) {
					if (elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_ONE 
							|| elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_MORE) {
						// next input can either be current element, or other elements, so 
						// mandatory scan can be stopped
						return new MandatoryScan(mandatories, true);
					} else if (elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
						MandatoryScan scan = elementSpec.scanMandatories(new HashSet<>(checkedRules));
						mandatories.addAll(scan.getMandatories());
						// next input can either be current element, or other elements, so 
						// mandatory scan can be stopped
						return new MandatoryScan(mandatories, true);
					} else {
						MandatoryScan scan = elementSpec.scanMandatories(new HashSet<>(checkedRules));
						mandatories.addAll(scan.getMandatories());
						// if internal of the element tells use to stop, let's stop 
						if (scan.isStop())
							return new MandatoryScan(mandatories, true);
					}
				}
				return new MandatoryScan(mandatories, false);
			} else {
				return MandatoryScan.stop();
			}
		} else {
			return MandatoryScan.stop();
		}
	}

	public Set<String> getLeadingLiterals(Set<String> checkedRules) {
		Set<String> leadingLiterals = new HashSet<>();
		if (!checkedRules.contains(name)) {
			checkedRules.add(name);
			for (AlternativeSpec alternative: alternatives) {
				for (ElementSpec elementSpec: alternative.getElements()) {
					leadingLiterals.addAll(elementSpec.getLeadingLiterals(new HashSet<>(checkedRules)));
					if (!elementSpec.matchesEmpty(new HashSet<String>()))
						break;
				}
			}
		}
		return leadingLiterals;
	}

	@Override
	public String toString() {
		List<String> alternativeStrings = new ArrayList<>();
		for (AlternativeSpec alternative: alternatives)
			alternativeStrings.add(alternative.toString());
		return StringUtils.join(alternativeStrings, " | ");
	}

}
