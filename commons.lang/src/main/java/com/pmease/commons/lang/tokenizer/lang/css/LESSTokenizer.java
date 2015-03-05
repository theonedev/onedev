package com.pmease.commons.lang.tokenizer.lang.css;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.pmease.commons.lang.tokenizer.StringStream;

public class LESSTokenizer extends AbstractCssTokenizer {

	private Map<String, Processor> TOKEN_HOOKS = new HashMap<>();
	
	private static Pattern PATTERN1 = Pattern.compile("^(charset|document|font-face|import|(-(moz|ms|o|webkit)-)?keyframes|media|namespace|page|supports)\\b");
	
	private static Pattern PATTERN2 = Pattern.compile("^[\\w\\\\\\-]");
	
	private static Pattern PATTERN3 = Pattern.compile("^\\s*:");
	
	public LESSTokenizer() {
		TOKEN_HOOKS.put("/", new Processor() {

			@Override
			public String process(StringStream stream, State state) {
				if (stream.eat("/").length() != 0) {
					stream.skipToEnd();
		            return "comment comment";
				} else if (stream.eat("*").length() != 0) {
		            state.tokenize = new TokenCComment();
		            return state.tokenize.process(stream, state);
				} else {
		            return "operator operator";
				}
			}
			
		});
		TOKEN_HOOKS.put("@", new Processor() {

			@Override
			public String process(StringStream stream, State state) {
		        if (!stream.match(PATTERN1, false).isEmpty()) 
		        	return "";
		        stream.eatWhile(PATTERN2);
		        if (!stream.match(PATTERN3, false).isEmpty())
		        	return "variable-2 variable-definition";
		        return "variable-2 variable";
			}
			
		});
		TOKEN_HOOKS.put("&", new Processor() {

			@Override
			public String process(StringStream stream, State state) {
		        return "atom atom";
			}
			
		});
	}
	
	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "less");
	}

	@Override
	protected boolean allowNested() {
		return true;
	}

	@Override
	protected Map<String, Processor> tokenHooks() {
		return TOKEN_HOOKS;
	}
	
}