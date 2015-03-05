package com.pmease.commons.lang.tokenizer.lang.clike;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pmease.commons.lang.tokenizer.StringStream;

public class ScalaTokenizer extends ClikeTokenizer {

	private static Set<String> KEYWORDS = wordsOf(
	/* scala */
	"abstract case catch class def do else extends false final finally for forSome if "
			+ "implicit import lazy match new null object override package private protected return "
			+ "sealed super this throw trait try trye type val var while with yield _ : = => <- <: "
			+ "<% >: # @ "
			+

			/* package scala */
			"assert assume require print println printf readLine readBoolean readByte readShort "
			+ "readChar readInt readLong readFloat readDouble "
			+

			"AnyVal App Application Array BufferedIterator BigDecimal BigInt Char Console Either "
			+ "Enumeration Equiv Error Exception Fractional Function IndexedSeq Integral Iterable "
			+ "Iterator List Map Numeric Nil NotNull Option Ordered Ordering PartialFunction PartialOrdering "
			+ "Product Proxy Range Responder Seq Serializable Set Specializable Stream StringBuilder "
			+ "StringContext Symbol Throwable Traversable TraversableOnce Tuple Unit Vector :: #:: "
			+

			/* package java.lang */
			"Boolean Byte Character CharSequence Class ClassLoader Cloneable Comparable "
			+ "Compiler Double Exception Float Integer Long Math Number Object Package Pair Process "
			+ "Runtime Runnable SecurityManager Short StackTraceElement StrictMath String "
			+ "StringBuffer System Thread ThreadGroup ThreadLocal Throwable Triple Void");

	private static Set<String> BLOCK_KEYWORDS = wordsOf("catch class do else finally for forSome if match switch try while");

	private static Set<String> ATOMS = Sets.newHashSet("true", "false", "null");

	private static Map<String, Processor> HOOKS = Maps.newHashMap();

	private static Pattern PATTERN = Pattern.compile("[\\w\\$_]");

	static {
		HOOKS.put("@", new Processor() {

			@Override
			public String process(StringStream stream, State state) {
				stream.eatWhile(PATTERN);
				return "meta";
			}

		});
		HOOKS.put("\"", new Processor() {

			@Override
			public String process(StringStream stream, State state) {
		        if (!stream.match("\"\"")) 
		        	return "";
		        state.tokenize = new TokenTripleString();
		        return state.tokenize.process(stream, state);
			}
			
		});
	}

	static class TokenTripleString implements Processor {

		@Override
		public String process(StringStream stream, State state) {
			boolean escaped = false;
			while (!stream.eol()) {
				if (!escaped && stream.match("\"\"\"")) {
					state.tokenize = null;
					break;
				}
				escaped = !stream.next().equals("\\") && !escaped;
			}
			return "string";
		}
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
	protected boolean indentStatements() {
		return false;
	}

	@Override
	protected Map<String, Processor> hooks() {
		return HOOKS;
	}

	@Override
	protected boolean multiLineStrings() {
		return true;
	}

	@Override
	public boolean accept(String fileName) {
		return acceptExtensions(fileName, "scala");
	}

}