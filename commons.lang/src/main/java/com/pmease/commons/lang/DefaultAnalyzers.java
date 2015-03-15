package com.pmease.commons.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;

import com.google.common.base.Joiner;

@Singleton
public class DefaultAnalyzers implements Analyzers {
	
	private final Set<Analyzer> analyzers;
	
	@Inject
	public DefaultAnalyzers(Set<Analyzer> analyzers) {
		this.analyzers = analyzers;
	}

	@Override
	public AnalyzeResult analyze(String fileContent, String fileName) {
		for (Analyzer analyzer: analyzers) {
			if (analyzer.accept(fileName))
				return analyzer.analyze(fileContent);
		}
		return null;
	}

	@Override
	public String getVersion() {
		List<String> versions = new ArrayList<>();
		
		for (Analyzer analyzer: analyzers) 
			versions.add(analyzer.getClass().getName() + ":" + analyzer.getVersion());
		
		Collections.sort(versions);
		return Joiner.on(",").join(versions);
	}

	@Override
	public String getVersion(String fileName) {
		for (Analyzer analyzer: analyzers) { 
			if (analyzer.accept(fileName))
				return analyzer.getVersion();
		}
		return null;
	}

}
