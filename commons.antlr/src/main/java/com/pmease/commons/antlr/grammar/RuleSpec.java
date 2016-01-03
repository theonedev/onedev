package com.pmease.commons.antlr.grammar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.antlr.codeassist.MandatoryScan;
import com.pmease.commons.antlr.grammar.ElementSpec.Multiplicity;

public class RuleSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final List<AlternativeSpec> alternatives;
	
	private Set<String> possiblePrefixes;
	
	private Boolean allowEmpty;
	
	private MandatoryScan mandatoryScan;
	
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

	private MandatoryScan doScanMandatories() {
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
					MandatoryScan scan = elementSpec.scanMandatories();
					mandatories.addAll(scan.getMandatories());
					// next input can either be current element, or other elements, so 
					// mandatory scan can be stopped
					return new MandatoryScan(mandatories, true);
				} else {
					MandatoryScan scan = elementSpec.scanMandatories();
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
	}
	
	public MandatoryScan scanMandatories() {
		if (mandatoryScan == null) {
			// initialize this to return a meaningful value in case this method is 
			// invoked recursively
			mandatoryScan = MandatoryScan.stop(); 
			
			mandatoryScan = doScanMandatories();
		}
		return mandatoryScan;
	}
	
	private Set<String> doGetPossiblePrefixes() {
		Set<String> possiblePrefixes = new LinkedHashSet<>();
		
		for (AlternativeSpec alternative: alternatives) {
			for (ElementSpec elementSpec: alternative.getElements()) {
				possiblePrefixes.addAll(elementSpec.getPossiblePrefixes());
				if (!elementSpec.isAllowEmpty())
					break;
			}
		}
		return possiblePrefixes;
	}

	public Set<String> getPossiblePrefixes() {
		if (possiblePrefixes == null) {
			// initialize this to return a meaningful value in case this method is 
			// invoked recursively
			possiblePrefixes = new LinkedHashSet<>();
			
			possiblePrefixes = doGetPossiblePrefixes();
		}
		return possiblePrefixes;
	}

	private boolean doIsAllowEmpty() {
		for (AlternativeSpec alternative: getAlternatives()) {
			boolean allowEmpty = true;
			for (ElementSpec elementSpec: alternative.getElements()) {
				if (!elementSpec.isAllowEmpty()) {
					allowEmpty = false;
					break;
				}
			}
			if (allowEmpty)
				return true;
		}
		return false;
	}
	
	public boolean isAllowEmpty() {
		if (allowEmpty == null) {
			// initialize this to return a meaningful value in case this method is 
			// invoked recursively
			allowEmpty = false; 

			allowEmpty = doIsAllowEmpty();
		}
		return allowEmpty;
	}

	@Override
	public String toString() {
		List<String> alternativeStrings = new ArrayList<>();
		for (AlternativeSpec alternative: alternatives)
			alternativeStrings.add(alternative.toString());
		return StringUtils.join(alternativeStrings, " | ");
	}

}
