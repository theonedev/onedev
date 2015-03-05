package com.pmease.commons.lang.tokenizer;

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
		return style.contains("cm-comment");
	}
	
	public boolean isKeyword() {
		return style.contains("cm-keyword");
	}
	
	public boolean isNumber() {
		return style.contains("cm-number");
	}
	
	public boolean isIdentifier() {
		return style.contains("cm-variable");
	}
	
	public boolean isString() {
		return style.contains("cm-string");
	}
	
	public boolean isMeta() {
		return style.contains("cm-meta");
	}
	
	public boolean isTag() {
		return style.contains("cm-tag");
	}
	
	public boolean isBracket() {
		return style.contains("cm-bracket");
	}
	
	public boolean isOperator() {
		return style.contains("cm-operator");
	}
	
	public boolean isAtom() {
		return style.contains("cm-atom");
	}
	
	public boolean isDef() {
		return style.contains("cm-def");
	}
	
	public boolean isQualifier() {
		return style.contains("cm-qualifier");
	}
	
	public boolean isAttribute() {
		return style.contains("cm-attribute");
	}
	
	public boolean isProperty() {
		return style.contains("cm-property");
	}
	
	public boolean isBuiltin() {
		return style.contains("cm-builtin");
	}
	
	public boolean isLink() {
		return style.contains("cm-link");
	}
	
	public boolean isError() {
		return style.contains("cm-error");
	}
	
	public boolean isNotCommentOrString() {
		return !isComment() && !isString();
	}
	
	public boolean isEof() {
		return style.length() == 0 && text.length() == 0;
	}
	
	public boolean isEol() {
		return style.length() == 0 && text.equals("\n");
	}
	
	public boolean isWhitespace() {
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
