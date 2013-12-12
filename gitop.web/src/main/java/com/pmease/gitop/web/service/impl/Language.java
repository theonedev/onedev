package com.pmease.gitop.web.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * 
 * type              - Either data, programming, markup, or nil
 * lexer             - An explicit lexer String (defaults to name)
 * aliases           - An Array of additional aliases (implicitly
 *                     includes name.downcase)
 * ace_mode          - A String name of Ace Mode (if available)
 * wrap              - Boolean wrap to enable line wrapping (default: false)
 * extension         - An Array of associated extensions
 * interpreter       - An Array of associated interpreters
 * primary_extension - A String for the main extension associated with
 *                    the language. Must be unique. Used when a Language is picked
 *                    from a dropdown and we need to automatically choose an
 *                    extension.
 * searchable        - Boolean flag to enable searching (defaults to true)
 * search_term       - Deprecated: Some languages maybe indexed under a
 *                     different alias. Avoid defining new exceptions.
 * color             - CSS hex color to represent the language.
 */
public class Language {

	public static enum Type {
		DATA, MARKUP, PROGRAMMING, PROSE
	}
	
	private String id;
	
	@JsonProperty
	private String type;
	
	@JsonProperty
	private String lexer;
	
	@JsonProperty
	private String[] aliases;
	
	@JsonProperty("ace_mode")
	private String aceMode;
	
	@JsonProperty
	private boolean wrap;
	
	@JsonProperty
	private String[] extensions;
	
	@JsonProperty
	private String[] interpreters;
	
	@JsonProperty("primary_extension")
	private String primaryExtension;
	
	@JsonProperty
	private boolean searchable = true;
	
	@JsonProperty
	private String[] filenames;
	
	@JsonProperty
	private String color;

	@JsonProperty("highlight_css")
	private String highlightCss;
	
	protected Language() {
	}
	
	public Language(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Type getLanguageType() {
		return type == null ? null : Type.valueOf(type.toUpperCase());
	}
	
	public String getLexer() {
		return lexer;
	}

	public void setLexer(String lexer) {
		this.lexer = lexer;
	}

	public String[] getAliases() {
		return aliases;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

	public String getAceMode() {
		return aceMode;
	}

	public void setAceMode(String aceMode) {
		this.aceMode = aceMode;
	}

	public boolean isWrap() {
		return wrap;
	}

	public void setWrap(boolean wrap) {
		this.wrap = wrap;
	}

	public String[] getExtensions() {
		return extensions;
	}

	public void setExtensions(String[] extensions) {
		this.extensions = extensions;
	}

	public String[] getInterpreters() {
		return interpreters;
	}

	public void setInterpreters(String[] interpreters) {
		this.interpreters = interpreters;
	}

	public String getPrimaryExtension() {
		return primaryExtension;
	}

	public void setPrimaryExtension(String primaryExtension) {
		this.primaryExtension = primaryExtension;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String[] getFilenames() {
		return filenames;
	}

	public void setFilenames(String[] filenames) {
		this.filenames = filenames;
	}

	public String getHighlightCss() {
		return highlightCss;
	}

	public void setHighlightCss(String highlightCss) {
		this.highlightCss = highlightCss;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
	
	@Override
	public String toString() {
		return getId();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Language)) 
			return false;
		
		Language rhs = (Language) other;
		return Objects.equal(id, rhs.id);
	}
}
