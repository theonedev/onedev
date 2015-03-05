package com.pmease.commons.lang.tokenizers.clike;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import com.pmease.commons.lang.StringStream;

public class ObjectiveCTokenizer extends ClikeTokenizer {

	private static Set<String> KEYWORDS = wordsOf(C_KEYWORDS, "inline restrict _Bool _Complex _Imaginery BOOL Class bycopy byref id IMP in " +
            "inout nil oneway out Protocol SEL self super atomic nonatomic retain copy readwrite readonly");

	private static Set<String> ATOMS = wordsOf("YES NO NULL NILL ON OFF");

	private static Map<String, Processor> HOOKS = Maps.newHashMap();

	private static Pattern PATTERN = Pattern.compile("[\\w\\$]");

	static {
		HOOKS.put("@", new Processor() {

			@Override
			public String process(StringStream stream, State state) {
				stream.eatWhile(PATTERN);
				return "keyword";
			}

		});
		HOOKS.put("#", new CppHook());
	}

	@Override
	protected Set<String> keywords() {
		return KEYWORDS;
	}

	@Override
	protected Set<String> atoms() {
		return ATOMS;
	}

	@Override
	protected Map<String, Processor> hooks() {
		return HOOKS;
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "m", "mm");
	}

}