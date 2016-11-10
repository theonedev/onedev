package com.gitplex.commons.lang.tokenizers.css;

import java.util.HashMap;
import java.util.Map;

import com.gitplex.commons.lang.tokenizers.StringStream;

public class CssTokenizer extends AbstractCssTokenizer {

	private Map<String, Processor> TOKEN_HOOKS = new HashMap<>();
	
	public CssTokenizer() {
		TOKEN_HOOKS.put("<", new Processor() {

			@Override
			public String process(StringStream stream, State state) {
		        if (!stream.match("!--")) 
		        	return "";
		        state.tokenize = new CssTokenizer.TokenSGMLComment();
		        return new TokenSGMLComment().process(stream, state);
			}
			
		});
		TOKEN_HOOKS.put("/", new Processor() {

			@Override
			public String process(StringStream stream, State state) {
		        if (stream.eat("*").length() == 0) 
		        	return "";
		        state.tokenize = new TokenCComment();
		        return state.tokenize.process(stream, state);
			}
			
		});
	}
	
	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "css");
	}

	@Override
	protected Map<String, Processor> tokenHooks() {
		return TOKEN_HOOKS;
	}
	
}