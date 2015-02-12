package com.pmease.commons.tokenizer;

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

	public String getStyle() {
		return style;
	}

	public String getText() {
		return text;
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
