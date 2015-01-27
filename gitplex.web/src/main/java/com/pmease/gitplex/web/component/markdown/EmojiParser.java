package com.pmease.gitplex.web.component.markdown;

import org.parboiled.Rule;
import org.pegdown.Parser;
import org.pegdown.plugins.InlinePluginParser;

public class EmojiParser extends Parser implements InlinePluginParser {

	private static final long TIMEOUT = 1000l;
	
	public EmojiParser() {
		super(ALL, TIMEOUT, DefaultParseRunnerProvider);
	}

	@Override
	public Rule[] inlinePluginRules() {
		return new Rule[] {Emoji()};
	}

	public Rule Emoji() {
		return NodeSequence(
				":", 
				OneOrMore(TestNot(AnyOf(": \t\f")), ANY), 
				push(new EmojiNode(match())), 
				":");
	}

}