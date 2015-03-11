package com.pmease.commons.lang.tokenizers.clike;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pmease.commons.lang.tokenizers.StringStream;

public class CSharpTokenizer extends ClikeTokenizer {

	private static Set<String> KEYWORDS = wordsOf("abstract as base break case catch checked class const continue"
			+ " default delegate do else enum event explicit extern finally fixed for"
			+ " foreach goto if implicit in interface internal is lock namespace new"
			+ " operator out override params private protected public readonly ref return sealed"
			+ " sizeof stackalloc static struct switch this throw try typeof unchecked"
			+ " unsafe using virtual void volatile while add alias ascending descending dynamic from get"
			+ " global group into join let orderby partial remove select set value var yield");

	private static Set<String> BLOCK_KEYWORDS = wordsOf("catch class do else finally for foreach if struct switch try while");

	private static Set<String> BUILT_IN = wordsOf("Boolean Byte Char DateTime DateTimeOffset Decimal Double"
			+ " Guid Int16 Int32 Int64 Object SByte Single String TimeSpan UInt16 UInt32"
			+ " UInt64 bool byte char decimal double short int long object"
			+ " sbyte float string ushort uint ulong");

	private static Set<String> ATOMS = Sets.newHashSet("true", "false",
			"null");

	private static Map<String, Processor> HOOKS = Maps.newHashMap();

	private static Pattern PATTERN = Pattern.compile("[\\w\\$_]");

	static {
		HOOKS.put("@", new Processor() {

			@Override
			public String process(StringStream stream, State state) {
				if (stream.eat("\"").length() != 0) {
					state.tokenize = new TokenAtString();
					return state.tokenize.process(stream, state);
				}
				stream.eatWhile(PATTERN);
				return "meta";
			}

		});
	}

	@Override
	protected Set<String> keywords() {
		return KEYWORDS;
	}

	@Override
	protected Set<String> blockKeywords() {
		return BLOCK_KEYWORDS;
	}

	@Override
	protected Set<String> atoms() {
		return ATOMS;
	}

	@Override
	protected Set<String> builtin() {
		return BUILT_IN;
	}

	@Override
	protected Map<String, Processor> hooks() {
		return HOOKS;
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "cs");
	}

}