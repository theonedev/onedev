package com.gitplex.server.search.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gitplex.commons.git.Blob;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsyntax.Token;
import com.gitplex.jsyntax.Tokenizer;
import com.gitplex.jsyntax.TokenizerRegistry;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

public class SourceContext {

	private static final CharMatcher NAMESPACE_SEPARATOR = new CharMatcher() {

		@Override
		public boolean matches(char c) {
			return !Character.isJavaIdentifierPart(c);
		}
		
	};
	
	private final Set<String> identifiers;
	
	public SourceContext(Blob blob) {
		Tokenizer tokenizer = TokenizerRegistry.getTokenizer(blob.getIdent().path);
		if (tokenizer != null) {
			identifiers = new HashSet<>();
			List<String> lines = Preconditions.checkNotNull(blob.getText()).getLines();
			for (List<Token> tokenizedLine: tokenizer.tokenize(lines)) {
				for (Token token: tokenizedLine) {
					if (token.isNotCommentOrString() && !token.isKeyword() && !token.isAtom())
						identifiers.add(token.getText());
				}
			}
		} else {
			identifiers = null;
		}
	}

	public boolean maybeUsing(Symbol symbol) {
		if (identifiers != null) {
			Set<String> parentNames = new HashSet<>();
			Symbol parent = symbol.getParent();
			while (parent != null) {
				if (parent.getName() != null) {
					parentNames.addAll(Splitter
							.on(NAMESPACE_SEPARATOR)
							.omitEmptyStrings()
							.splitToList(parent.getName()));
				}
				parent = parent.getParent();
			}
			return identifiers.containsAll(parentNames);
		} else {
			return true;
		}
	}
	
}
