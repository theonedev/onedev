package com.pmease.commons.lang.extractors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Joiner;

@Singleton
public class DefaultExtractors implements Extractors {
	
	private final Set<Extractor> extractorSet;
	
	@Inject
	public DefaultExtractors(Set<Extractor> extractorSet) {
		this.extractorSet = extractorSet;
	}

	@Override
	public Extractor getExtractor(String fileName) {
		for (Extractor extractor: extractorSet) {
			if (extractor.accept(fileName))
				return extractor;
		}
		return null;
	}

	@Override
	public String getVersion() {
		List<String> versions = new ArrayList<>();
		
		for (Extractor extractor: extractorSet) 
			versions.add(extractor.getClass().getName() + ":" + extractor.getVersion());
		
		Collections.sort(versions);
		return Joiner.on(",").join(versions);
	}

}
