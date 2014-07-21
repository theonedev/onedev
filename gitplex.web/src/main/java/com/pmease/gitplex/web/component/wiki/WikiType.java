package com.pmease.gitplex.web.component.wiki;

public enum WikiType {
	MARKDOWN("Markdown"), 
	CONFLUENCE("Confluence"), 
	TEXTILE("Textile"), 
	MEDIAWIKI("MediaWiki"), 
	TRACWIKI("TracWiki"), 
	TWIKI("TWiki");
	
	private final String language;
	
	WikiType(final String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}
	
}
