package com.pmease.gitplex.web.component.markdown;

import org.parboiled.Rule;
import org.pegdown.Parser;
import org.pegdown.plugins.InlinePluginParser;

public class MentionParser extends Parser implements InlinePluginParser {

	private static final long TIMEOUT = 1000l;
	
	public MentionParser() {
		super(ALL, TIMEOUT, DefaultParseRunnerProvider);
	}

	@Override
	public Rule[] inlinePluginRules() {
		return new Rule[] {Mention()};
	}

	public Rule Mention() {
		return NodeSequence(
				"@", 
				OneOrMore(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_')), 
				push(new MentionNode(match())) 
				);
	}
	
}