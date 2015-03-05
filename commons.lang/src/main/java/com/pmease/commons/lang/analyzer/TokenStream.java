package com.pmease.commons.lang.analyzer;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.lang.tokenizer.Token;
import com.pmease.commons.lang.tokenizer.TokenizedLine;

public class TokenStream {
	
	private static final Token EOF = new Token("", "");
	
	private static final Token EOL = new Token("", "\n");
	
	private final List<LineAwareToken> tokens;
	
	private int tokenPos; // 0-indexed position of current token 
	
	public TokenStream(List<TokenizedLine> lines, boolean preserveWhitespace) {
		tokens = new ArrayList<>();
		int linePos = 0;
		for (TokenizedLine line: lines) {
			for (Token token: line.getTokens()) {
				if (token.isNotCommentOrString() && (preserveWhitespace || !token.isWhitespace()))
					tokens.add(new LineAwareToken(token, linePos));
			}
			if (preserveWhitespace)
				tokens.add(new LineAwareToken(EOL, linePos));
			linePos++;
		}
	}
	
	private Token getToken(int pos) {
		if (pos >=0 && pos < tokens.size())
			return tokens.get(pos);
		else
			return EOF;
	}
	
	public Token current() {
		return getToken(tokenPos);
	}

	public int tokenPos() {
		return tokenPos;
	}

	public Token next() {
		tokenPos++;
		return current();
	}
	
	public Token lookAhead(int ahead) {
		return getToken(tokenPos+ahead);
	}
	
	public Token lookBehind(int behind) {
		return getToken(tokenPos-behind);
	}

	public Token next(String...anyOf) {
		Token token = next();
		while(!token.isEof()) {
			for (String text: anyOf) {
				if (text.equals(token.text()))
					return token;
			}
			token = next();
		}
		return token;
	}

	private Token nextBalanced(String open, String close) {
		int nestingLevel = 1;
		Token balanced = next(open, close);
		while (!balanced.isEof()) {
			if (balanced.text().equals(close)) {
				if (--nestingLevel == 0)
					return balanced;
			} else if (balanced.text().equals(open)) {
				nestingLevel++;
			}
			balanced = next(open, close);
		}
		return balanced;
	}
	
	public Token nextBalanced(Token token) {
		if (token.text().equals("{")) {
			return nextBalanced("{", "}");
		} else if (token.text().equals("[")) {
			return nextBalanced("[", "]");
		} else if (token.text().equals("<")) {
			return nextBalanced("<", ">");
		} else if (token.text().equals("(")) {
			return nextBalanced("(", ")");
		} else {
			throw new IllegalStateException("Not a balanceable token: " + token.text());
		}
	}
	
}
