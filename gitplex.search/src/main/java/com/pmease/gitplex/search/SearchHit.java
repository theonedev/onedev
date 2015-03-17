package com.pmease.gitplex.search;

import org.apache.lucene.search.ScoreDoc;

public class SearchHit {

	private final String path;
	
	private final int line;
	
	private final int posInLine;

	private final String description;
	
	private final ScoreDoc doc;
	
	public SearchHit(String path, int line, int posInLine, String description, ScoreDoc doc) {
		this.path = path;
		this.line = line;
		this.posInLine = posInLine;
		this.description = description;
		this.doc = doc;
	}
	
	public String getPath() {
		return path;
	}

	public int getLine() {
		return line;
	}

	public int getPosInLine() {
		return posInLine;
	}

	public String getDescription() {
		return description;
	}

	public ScoreDoc getDoc() {
		return doc;
	}

}
