package com.pmease.commons.lang;

import java.io.Serializable;

import com.google.common.base.Objects;

public class Token implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String style;
	
	private final String text;
	
	public Token(String style, String text) {
		this.style = style;
		this.text = text;
	}

	public String style() {
		return style;
	}

	public String text() {
		return text;
	}

	public boolean isComment() {
		return style.contains("comment");
	}
	
	public boolean isKeyword() {
		return style.contains("keyword");
	}
	
	public boolean isNumber() {
		return style.contains("number");
	}
	
	public boolean isIdentifier() {
		return style.contains("variable");
	}
	
	public boolean isString() {
		return style.contains("string");
	}
	
	public boolean isMeta() {
		return style.contains("meta");
	}
	
	public boolean isTag() {
		return style.contains("tag");
	}
	
	public boolean isBracket() {
		return style.contains("bracket");
	}
	
	public boolean isOperator() {
		return style.contains("operator");
	}
	
	public boolean isAtom() {
		return style.contains("atom");
	}
	
	public boolean isDef() {
		return style.contains("def");
	}
	
	public boolean isQualifier() {
		return style.contains("qualifier");
	}
	
	public boolean isAttribute() {
		return style.contains("attribute");
	}
	
	public boolean isProperty() {
		return style.contains("property");
	}
	
	public boolean isBuiltin() {
		return style.contains("builtin");
	}
	
	public boolean isLink() {
		return style.contains("link");
	}
	
	public boolean isError() {
		return style.contains("error");
	}
	
	public boolean isEof() {
		return style.length() == 0 && text.length() == 0;
	}
	
	public boolean isEol() {
		return style.length() == 0 && text.equals("\n");
	}
	
	public boolean is(String... anySymbols) {
		if (isComment() || isString())
			return false;
		for (String symbol: anySymbols) {
			if (symbol.equals(text))
				return true;
		}
		return false;
	}
	
	public  boolean isWhitespace() {
		if (style.length() != 0)
			return false;
		for (char ch: text.toCharArray()) {
			if (ch != ' ' && ch != '\n' && ch != '\r' && ch != '\t')
				return false;
		}
		return true;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof Token))
			return false;
		if (this == other)
			return true;
		Token otherToken = (Token) other;
		return Objects.equal(style, otherToken.style) && Objects.equal(text, otherToken.text);
	}

	public int hashCode() {
		return Objects.hashCode(style, text);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(Token.class).add("text", text).add("style", style).toString();
	}
	
}
