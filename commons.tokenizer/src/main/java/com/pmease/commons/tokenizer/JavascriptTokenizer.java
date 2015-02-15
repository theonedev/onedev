package com.pmease.commons.tokenizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

public abstract class JavascriptTokenizer extends AbstractTokenizer<JavascriptTokenizer.State> {

	static class State {
		String type = "";
		String content;
		Processor tokenize;
		String lastType;
		int fatArrowAt = -1;
		Var localVars;
		Var globalVars;
		Context context;
		Stack<Combinator> cc;
		String style;
		String marked;
		JSLexical lexical;
		int indented;
	}
	
	static class Keyword {
		final String type, style;
		
		Keyword(String type, String style) {
			this.type = type;
			this.style = style;
		}
	}
	
	static interface Processor {
		String process(StringStream stream, State state);
	}
	
	static Keyword kw(String type) {
		return new Keyword(type, "keyword");
	}
	
	static Keyword A = kw("keyword a");
	static Keyword B = kw("keyword b");
	static Keyword C = kw("keyword c");
	static Keyword operator = kw("operator");
	static Keyword atom = new Keyword("atom", "atom");

	final Map<String, Keyword> keywords = new HashMap<>();
	
	final boolean isTS;
	
	public JavascriptTokenizer(boolean typescript) {
		isTS = typescript;
		
		keywords.put("if", kw("if"));
		keywords.put("while", A);
		keywords.put("with", A);
		keywords.put("else", B);
		keywords.put("do", B);
		keywords.put("try", B);
		keywords.put("finally", B);
		keywords.put("return", C);
		keywords.put("break", C);
		keywords.put("continue", C);
		keywords.put("new", C);
		keywords.put("delete", C);
		keywords.put("throw", C);
		keywords.put("debugger", C);
		keywords.put("var", kw("var"));
		keywords.put("const", kw("var"));
		keywords.put("let", kw("var"));
		keywords.put("function", kw("function"));
		keywords.put("catch", kw("catch"));
		keywords.put("for", kw("for"));
		keywords.put("switch", kw("switch"));
		keywords.put("case", kw("case"));
		keywords.put("default", kw("default"));
		keywords.put("in", operator);
		keywords.put("typeof", operator);
		keywords.put("instanceof", operator);
		keywords.put("true", atom);
		keywords.put("false", atom);
		keywords.put("null", atom);
		keywords.put("undefined", atom);
		keywords.put("NaN", atom);
		keywords.put("Infinity", atom);
		keywords.put("this", kw("this"));
		keywords.put("module", kw("module"));
		keywords.put("class", kw("class"));
		keywords.put("super", kw("atom"));
		keywords.put("yield", C);
		keywords.put("export", kw("export"));
		keywords.put("import", kw("import"));
		keywords.put("extends", C);
		
		if (isTS) {
			Keyword type = new Keyword("variable", "variable-3");
			keywords.put("interface", kw("interface"));
			keywords.put("extends", kw("extends"));
			keywords.put("constructor", kw("constructor"));
			keywords.put("public", kw("public"));
			keywords.put("private", kw("private"));
			keywords.put("protected", kw("protected"));
			keywords.put("static", kw("static"));
			keywords.put("string", type);
			keywords.put("number", type);
			keywords.put("bool", type);
			keywords.put("any", type);
		}
	}
	
	static Pattern wordRE = Pattern.compile("[\\w$\\xa1-\\uffff]");
	
	static Pattern isOperatorChar = Pattern.compile("[+\\-*&%=<>!?|~^]"); 
	
	static Pattern isJsonldKeyword = Pattern.compile("^@(context|id|value|language|type|container|list|set|reverse|index|base|vocab|graph)\"");

	void readRegexp(StringStream stream) {
	    boolean escaped = false;
	    String next;
	    boolean inSet = false;
	    while ((next = stream.next()).length() != 0) {
	    	if (!escaped) {
	    		if (next.equals("/") && !inSet) 
	    			return;
	        if (next.equals("[")) 
	        	inSet = true;
	        else if (inSet && next.equals("]")) 
	        	inSet = false;
	    	}
	    	escaped = !escaped && next.equals("\\");
	    }
	}
	
	String ret(String tp, String style, String cont, State state) {
		state.type = tp; state.content = cont;
		return style;
	}
	
	static Pattern TOKEN_BASE_PATTERN1 = Pattern.compile("^\\d+(?:[eE][+\\-]?\\d+)?");
	
	static Pattern TOKEN_BASE_PATTERN2 = Pattern.compile("[\\[\\]{}\\(\\),;\\:\\.]");
	
	static Pattern TOKEN_BASE_PATTERN3 = Pattern.compile("x", Pattern.CASE_INSENSITIVE);
	
	static Pattern TOKEN_BASE_PATTERN4 = Pattern.compile("[\\da-f]", Pattern.CASE_INSENSITIVE);
	
	static Pattern TOKEN_BASE_PATTERN5 = Pattern.compile("\\d");
	
	static Pattern TOKEN_BASE_PATTERN6 = Pattern.compile("^\\d*(?:\\.\\d*)?(?:[eE][+\\-]?\\d+)?");
	
	static Pattern TOKEN_BASE_PATTERN7 = Pattern.compile("^[\\[{}\\(,;:]$");
	
	static Pattern TOKEN_BASE_PATTERN8 = Pattern.compile("[gimy]");
	
	class TokenBase implements Processor {

		@Override
		public String process(StringStream stream, State state) {
			String ch = stream.next();
		    if (ch.equals("\"") || ch.equals("'")) {
		    	state.tokenize = new TokenString(ch);
		    	return state.tokenize.process(stream, state);
		    } else if (ch.equals(".") && !stream.match(TOKEN_BASE_PATTERN1).isEmpty()) {
		    	return ret("number", "number", "", state);
		    } else if (ch.equals(".") && stream.match("..")) {
		    	return ret("spread", "meta", "", state);
		    } else if (TOKEN_BASE_PATTERN2.matcher(ch).find()) {
		    	return ret(ch, "", "", state);
		    } else if (ch.equals("=") && stream.eat(">").length() != 0) {
		    	return ret("=>", "operator", "", state);
		    } else if (ch.equals("0") && stream.eat(TOKEN_BASE_PATTERN3).length() != 0) {
		    	stream.eatWhile(TOKEN_BASE_PATTERN4);
		    	return ret("number", "number", "", state);
		    } else if (TOKEN_BASE_PATTERN5.matcher(ch).find()) {
		    	stream.match(TOKEN_BASE_PATTERN6);
		    	return ret("number", "number", "", state);
		    } else if (ch.equals("/")) {
		    	if (stream.eat("*").length() != 0) {
		    		state.tokenize = new TokenComment();
		    		return state.tokenize.process(stream, state);
		    	} else if (stream.eat("/").length() != 0) {
		    		stream.skipToEnd();
		    		return ret("comment", "comment", "", state);
		    	} else if (state.lastType.equals("operator") || state.lastType.equals("keyword c") ||
		               state.lastType.equals("sof") || TOKEN_BASE_PATTERN7.matcher(state.lastType).find()) {
		    		readRegexp(stream);
		    		stream.eatWhile(TOKEN_BASE_PATTERN8); // 'y' is "sticky" option in Mozilla
		    		return ret("regexp", "string-2", "", state);
		    	} else {
		    		stream.eatWhile(isOperatorChar);
		    		return ret("operator", "operator", stream.current(), state);
		    	}
		    } else if (ch.equals("`")) {
		    	state.tokenize = new TokenQuasi();
		    	return state.tokenize.process(stream, state);
		    } else if (ch.equals("#")) {
		    	stream.skipToEnd();
		    	return ret("error", "error", "", state);
		    } else if (isOperatorChar.matcher(ch).find()) {
		    	stream.eatWhile(isOperatorChar);
		    	return ret("operator", "operator", stream.current(), state);
		    } else if (wordRE.matcher(ch).find()) {
		    	stream.eatWhile(wordRE);
		    	String word = stream.current();
		    	Keyword known = keywords.get(word);
		    	return (known!=null && !state.lastType.equals(".")) ? ret(known.type, known.style, word, state) :
		                     ret("variable", "variable", word, state);
		    }
		    return "";
		}
		
	}
	
	class TokenString implements Processor {
		final String quote;
		
		TokenString(String quote) {
			this.quote = quote;
		}
		
		@Override
		public String process(StringStream stream, State state) {
		      boolean escaped = false;
		      String next;
		      if (jsonldMode() && stream.peek().equals("@") && !stream.match(isJsonldKeyword).isEmpty()){
		    	  state.tokenize = new TokenBase();
		    	  return ret("jsonld-keyword", "meta", "", state);
		      }
		      while ((next = stream.next()).length() != 0) {
		    	  if (next.equals(quote) && !escaped) 
		    		  break;
		    	  escaped = !escaped && next.equals("\\");
		      }
		      if (!escaped) 
		    	  state.tokenize = new TokenBase();
		      return ret("string", "string", "", state);
		}
		
	}
	
	class TokenComment implements Processor {

		@Override
		public String process(StringStream stream, State state) {
		    boolean maybeEnd = false;
		    String ch;
		    while ((ch = stream.next()).length() != 0) {
		    	if (ch.equals("/") && maybeEnd) {
		    		state.tokenize = new TokenBase();
		    		break;
		    	}
		    	maybeEnd = (ch.equals("*"));
		    }
		    return ret("comment", "comment", "", state);
		}
		
	}
	
	class TokenQuasi implements Processor {

		@Override
		public String process(StringStream stream, State state) {
		    boolean escaped = false;
		    String next;
		    while ((next = stream.next()).length() != 0) {
		    	if (!escaped && (next.equals("`") || next.equals("$") && stream.eat("{").length() != 0)) {
		    		state.tokenize = new TokenBase();
		    		break;
		    	}
		    	escaped = !escaped && next.equals("\\");
		    }
		    return ret("quasi", "string-2", stream.current(), state);
		}
		
	}

	static String brackets = "([{}])";
	
	static Pattern FIND_FAT_ARROW_PATTERN = Pattern.compile("[\"'\\/]");
	
	void findFatArrow(StringStream stream, State state) {
	    if (state.fatArrowAt != -1) 
	    	state.fatArrowAt = -1;
	    
	    int arrow = stream.string().indexOf("=>", stream.start());
	    if (arrow < 0) 
	    	return;

	    int depth = 0;
	    boolean sawSomething = false;
	    int pos;
	    for (pos = arrow - 1; pos >= 0; --pos) {
	    	String ch = String.valueOf(stream.string().charAt(pos));
	    	int bracket = brackets.indexOf(ch);
	    	if (bracket >= 0 && bracket < 3) {
	    		if (depth==0) { 
	    			++pos; break; 
	    		}
	    		if (--depth == 0) 
	    			break;
	    	} else if (bracket >= 3 && bracket < 6) {
	    		++depth;
	    	} else if (wordRE.matcher(ch).find()) {
	    		sawSomething = true;
	    	} else if (FIND_FAT_ARROW_PATTERN.matcher(ch).find()) {
	    		return;
	      	} else if (sawSomething && depth==0) {
	      		++pos;
	      		break;
	      	}
	    }
	    if (sawSomething && depth==0) 
	    	state.fatArrowAt = pos;
	}
	
	static Set<String> atomicTypes = Sets.newHashSet(
			"atom", "number", "variable", "string", "regexp", "this", "jsonld-keyword");

	class JSLexical {
	    int indented;
	    int column;
	    String type;
	    JSLexical prev;
	    String info;
	    Boolean align;
	    int pos;
	    
	    JSLexical(int indented, int column, String type, Boolean align, JSLexical prev, String info) {
	        this.indented = indented;
	        this.column = column;
	        this.type = type;
	        this.prev = prev;
	        this.info = info;
	        if (align != null) 
	        	this.align = align;
	    }
	}
	
	class Var {
		String name;
		Var next;
		
		Var(String name, Var next) {
			this.name = name;
			this.next = next;
		}
	}
	
	class Context {
		Var vars;
		Context prev;
		
		Context(Var vars, Context prev) {
			this.vars = vars;
			this.prev = prev;
		}
	}
	
	boolean inScope(State state, String varname) {
	    for (Var v = state.localVars; v != null; v = v.next)
	    	if (v.name.equals(varname)) 
	    		return true;
	    for (Context cx = state.context; cx != null; cx = cx.prev) {
	    	for (Var v = cx.vars; v!=null; v = v.next)
	    		if (v.name.equals(varname)) return true;
	    }
	    return false;
	}
	
	static abstract class Combinator {
		
		abstract boolean combine(StringStream stream, State state, String type, String value);

		boolean lex() {
			return false;
		}
	}
	
	class Expression extends Combinator {

		@Override
		public boolean combine(StringStream stream, State state, String type, String content) {
		    return expressionInner(stream, state, type, false);
		}
	}
	
	class ExpressionNoComma extends Combinator {

		@Override
		public boolean combine(StringStream stream, State state, String type, String content) {
			return expressionInner(stream, state, type, true);
		}
		
	}
	
	boolean pass(State state, Combinator... combinators) {
		for (int i = combinators.length - 1; i >= 0; i--) 
			state.cc.push(combinators[i]);		
		return false;
	}
	
	boolean cont(State state, Combinator... combinators) {
	    pass(state, combinators);
	    return true;
	}
	
	boolean inList(Var list, String varname) {
		for (Var v = list; v!=null; v = v.next) {
			if (v.name.equals(varname)) 
				return true;
		}
		return false;
	}
	
	void register(State state, String varname) {
	    if (state.context != null) {
	    	state.marked = "def";
	    	if (inList(state.localVars, varname)) 
	    		return;
	    	state.localVars = new Var(varname, state.localVars);
	    } else {
	    	if (inList(state.globalVars, varname)) 
	    		return;
	    	if (globalVars() != null)
	    		state.globalVars = new Var(varname, state.globalVars);
	    }
	}
	
	final Var defaultVars = new Var("this", new Var("arguments", null));
	
	class PushContext extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type, String value) {
		    state.context = new Context(state.localVars, state.context);
		    state.localVars = defaultVars;
		    return false;
		}
		
	}
	
	class PopContext extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type, String value) {
		    state.localVars = state.context.vars;
		    state.context = state.context.prev;
			return false;
		}
		
	}
	
	class PushLex extends Combinator {
		
		final String type;
		
		final String info;
		
		PushLex(String type, String info) {
			this.type = type;
			this.info = info;
		}

		@Override
		boolean lex() {
			return true;
		}

		@Override
		boolean combine(StringStream stream, State state, String type, String content) {
			int indent = state.indented;
			if (state.lexical.type.equals("stat")) 
				indent = state.lexical.indented;
			else for (JSLexical outer = state.lexical; outer!=null && outer.type.equals(")") && outer.align!=null&&outer.align; outer = outer.prev)
				indent = outer.indented;
			state.lexical = new JSLexical(indent, stream.column(), this.type, null, state.lexical, info);
			return false;
		}
		
	}
	
	class PopLex extends Combinator {
		
		@Override
		boolean lex() {
			return true;
		}
		
		@Override
		boolean combine(StringStream stream, State state, String type, String content) {
			if (state.lexical.prev != null) {
				if (state.lexical.type.equals(")"))
					state.indented = state.lexical.indented;
				state.lexical = state.lexical.prev;
		    }
			return false;
		}
		
	}
	
	class Expect extends Combinator {

		final String wanted;
		
		Expect(String wanted) {
			this.wanted = wanted;
		}
		
		@Override
		boolean combine(StringStream stream, State state, String type, String content) {
		      if (type.equals(wanted)) 
		    	  return cont(state);
		      else if (wanted.equals(";")) 
		    	  return pass(state);
		      else 
		    	  return cont(state, new Expect(wanted));
		}
		
	}
	
	class Statement extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type, String value) {
		    if (type.equals("var")) 
		    	return cont(state, new PushLex("vardef", String.valueOf(value.length())), new Vardef(), new Expect(";"), new PopLex());
		    if (type.equals("keyword a")) 
		    	return cont(state, new PushLex("form", ""), new Expression(), new Statement(), new PopLex());
		    if (type.equals("keyword b")) 
		    	return cont(state, new PushLex("form", ""), new Statement(), new PopLex());
		    if (type.equals("{")) 
		    	return cont(state, new PushLex("}", ""), new Block(), new PopLex());
		    if (type.equals(";")) 
		    	return cont(state);
		    if (type.equals("if")) {
		    	if (state.lexical.info.equals("else") && (state.cc.get(state.cc.size() - 1) instanceof PopLex))
		    		state.cc.pop().combine(stream, state, "", "");
		    	return cont(state, new PushLex("form", ""), new Expression(), new Statement(), new PopLex(), new Maybeelse());
		    }
		    if (type.equals("function")) 
		    	return cont(state, new Functiondef());
		    if (type.equals("for")) 
		    	return cont(state, new PushLex("form", ""), new Forspec(), new Statement(), new PopLex());
		    if (type.equals("variable")) 
		    	return cont(state, new PushLex("stat", ""), new Maybelabel());
		    if (type.equals("switch")) 
		    	return cont(state, new PushLex("form", ""), new Expression(), new PushLex("}", "switch"), new Expect("{"), new Block(), new PopLex(), new PopLex());
		    if (type.equals("case")) 
		    	return cont(state, new Expression(), new Expect(":"));
		    if (type.equals("default")) 
		    	return cont(state, new Expect(":"));
		    if (type.equals("catch")) 
		    	return cont(state, new PushLex("form", ""), new PushContext(), new Expect("("), new Funarg(), new Expect(")"), new Statement(), new PopLex(), new PopContext());
		    if (type.equals("module")) 
		    	return cont(state, new PushLex("form", ""), new PushContext(), new AfterModule(), new PopContext(), new PopLex());
		    if (type.equals("class")) 
		    	return cont(state, new PushLex("form", ""), new ClassName(), new PopLex());
		    if (type.equals("export")) 
		    	return cont(state, new PushLex("form", ""), new AfterExport(), new PopLex());
		    if (type.equals("import")) 
		    	return cont(state, new PushLex("form", ""), new AfterImport(), new PopLex());
		    return pass(state, new PushLex("stat", ""), new Expression(), new Expect(";"), new PopLex());
		}
		
	}
	
	boolean expressionInner(StringStream stream, State state, String type, boolean noComma) {
		if (state.fatArrowAt == stream.start()) {
			Combinator body = noComma ? new ArrowBodyNoComma() : new ArrowBody();
	        if (type.equals("(")) 
	        	return cont(state, new PushContext(), new PushLex(")", ""), new Commasep(new PatternCombinator(), ")"), new PopLex(), new Expect("=>"), body, new PopContext());
	        else if (type.equals("variable")) 
	        	return pass(state, new PushContext(), new PatternCombinator(), new Expect("=>"), body, new PopContext());
	      }

	      Combinator maybeop = noComma ? new MaybeoperatorNoComma() : new MaybeoperatorComma();
	      if (atomicTypes.contains(type)) 
	    	  return cont(state, maybeop);
	      if (type.equals("function")) 
	    	  return cont(state, new Functiondef(), maybeop);
	      if (type.equals("keyword c")) 
	    	  return cont(state, noComma ? new MaybeexpressionNoComma() : new Maybeexpression());
	      if (type.equals("(")) 
	    	  return cont(state, new PushLex(")", ""), new Maybeexpression(), new Comprehension(), new Expect(")"), new PopLex(), maybeop);
	      if (type.equals("operator") || type.equals("spread")) 
	    	  return cont(state, noComma ? new ExpressionNoComma() : new Expression());
	      if (type.equals("[")) 
	    	  return cont(state, new PushLex("]", ""), new ArrayLiteral(), new PopLex(), maybeop);
	      if (type.equals("{")) 
	    	  return contCommasep(state, new Objprop(), "}", "", maybeop);
	      if (type.equals("quasi")) { 
	    	  return pass(state, new Quasi(), maybeop); 
	      }
	      return cont(state);
	}
	
	String parseJS(State state, String style, String type, String content, StringStream stream) {
	    Stack<Combinator> cc = state.cc;
	    // Communicate our context to the combinators.
	    // (Less wasteful than consing up a hundred closures on every call.)
	    state.marked = ""; state.style = style;

	    if (state.lexical.align == null)
	    	state.lexical.align = true;

	    while(true) {
	    	Combinator combinator = !state.cc.isEmpty()? cc.pop() : jsonMode() ? new Expression() : new Statement();
	    	if (combinator.combine(stream, state, type, content)) {
	    		while(!state.cc.isEmpty() && state.cc.get(state.cc.size() - 1).lex())
	    			cc.pop().combine(stream, state, "", "");
	    		if (state.marked.length()!=0) 
	    			return state.marked;
	    		if (type.equals("variable") && inScope(state, content)) 
	    			return "variable-2";
	    		return style;
	    	}
	    }
	}
	
	static Pattern MAYBE_EXPRESSION_PATTERN = Pattern.compile("[;\\}\\)\\],]");
	
	class Maybeexpression extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type, String value) {
		    if (MAYBE_EXPRESSION_PATTERN.matcher(type).find()) 
		    	return pass(state);
		    return pass(state, new Expression());
		}
	}
	
	class MaybeexpressionNoComma extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type, String value) {
		    if (MAYBE_EXPRESSION_PATTERN.matcher(type).find()) 
		    	return pass(state);
		    return pass(state, new ExpressionNoComma());
		}
	}

	class MaybeoperatorComma extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type, String value) {
		    if (type.equals(",")) 
		    	return cont(state, new Expression());
		    return new MaybeoperatorNoComma(false).combine(stream, state, type, value);
		}
		
	}
	
	static Pattern MAYBE_OPERATOR_NOCOMMA_PATTERN = Pattern.compile("\\+\\+|--");
	
	class MaybeoperatorNoComma extends Combinator {

		final boolean noComma;
		
		MaybeoperatorNoComma() {
			noComma = true;
		}
		
		MaybeoperatorNoComma(boolean noComma) {
			this.noComma = noComma;
		}
		
		@Override
		boolean combine(StringStream stream, State state, String type, String value) {
		    Combinator me = noComma == false ? new MaybeoperatorComma() : new MaybeoperatorNoComma();
		    Combinator expr = noComma == false ? new Expression() : new ExpressionNoComma();
		    if (type.equals("=>")) 
		    	return cont(state, new PushContext(), noComma ? new ArrowBodyNoComma() : new ArrowBody(), new PopContext());
		    if (type.equals("operator")) {
		    	if (MAYBE_OPERATOR_NOCOMMA_PATTERN.matcher(value).find()) 
		    		return cont(state, me);
		    	if (value.equals("?")) 
		    		return cont(state, new Expression(), new Expect(":"), expr);
		    	return cont(state, expr);
		    }
		    if (type.equals("quasi")) {return pass(state, new Quasi(), me); }
		    if (type.equals(";")) return false;
		    if (type.equals("(")) return contCommasep(state, new ExpressionNoComma(), ")", "call", me);
		    if (type.equals(".")) return cont(state, new Property(), me);
		    if (type.equals("[")) return cont(state, new PushLex("]", ""), new Maybeexpression(), new Expect("]"), new PopLex(), me);
		    return false;
		}
		
	}
	
	class Quasi extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type, String value) {
		    if (!type.equals("quasi")) 
		    	return pass(state);
		    if (!value.substring(value.length() - 2).equals("${")) 
		    	return cont(state, new Quasi());
		    return cont(state, new Expression(), new ContinueQuasi());
		}
		
	}
	
	class ContinueQuasi extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type, String value) {
		    if (type.equals("}")) {
		    	state.marked = "string-2";
		        state.tokenize = new TokenQuasi();
		        return cont(state, new Quasi());
		    }
		    return false;
		}
		
	}
	
	class ArrowBody extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type, String value) {
		    findFatArrow(stream, state);
		    return pass(state, type.equals("{") ? new Statement() : new Expression());
		}
		
	}
	
	class ArrowBodyNoComma extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    findFatArrow(stream, state);
		    return pass(state, type.equals("{") ? new Statement(): new ExpressionNoComma());
		}
		
	}
	
	class Maybelabel extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals(":")) 
		    	return cont(state, new PopLex(), new Statement());
		    return pass(state, new MaybeoperatorComma(), new Expect(";"), new PopLex());
		}
		
	}
	
	class Property extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			if (type.equals("variable")) {
				state.marked = "property"; 
				return cont(state);
			}
			return false;
		}
		
	}
	
	class Objprop extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("variable") || state.style.equals("keyword")) {
		    	state.marked = "property";
		        if (value.equals("get") || value.equals("set")) 
		        	return cont(state, new GetterSetter());
		        return cont(state, new Afterprop());
		    } else if (type.equals("number") || type.equals("string")) {
		    	state.marked = jsonldMode() ? "property" : (state.style + " property");
		        return cont(state, new Afterprop());
		    } else if (type.equals("jsonld-keyword")) {
		    	return cont(state, new Afterprop());
		    } else if (type.equals("[")) {
		    	return cont(state, new Expression(), new Expect("]"), new Afterprop());
		    }
		    return false;
		}
		
	}
	
	class GetterSetter extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (!type.equals("variable")) 
		    	return pass(state, new Afterprop());
		    state.marked = "property";
		    return cont(state, new Functiondef());
		}
		
	}
	
	class Afterprop extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals(":")) 
		    	return cont(state, new ExpressionNoComma());
		    if (type.equals("(")) 
		    	return pass(state, new Functiondef());
		    return false;
		}
		
	}
	
	class Commasep extends Combinator {

		final Combinator what;
		
		final String end;
		
		Commasep(Combinator what, String end) {
			this.what = what;
			this.end = end;
		}
		
		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		      if (type.equals(end)) 
		    	  return cont(state);
		      return pass(state, what, new Combinator() {

				@Override
				boolean combine(StringStream stream, State state, String type,
						String value) {
				      if (type.equals(",")) {
				    	  JSLexical lex = state.lexical;
				    	  if (lex.info.equals("call")) 
				    		  lex.pos = lex.pos + 1;
				          return cont(state, what, this);
				      }
				      if (type.equals(end)) 
				    	  return cont(state);
				      return cont(state, new Expect(end));
				}
		    	  
		      });
		}
		
	}
	
	boolean contCommasep(State state, Combinator what, String end, String info, Combinator...combinators) {
	    for (Combinator combinator: combinators)
	        state.cc.push(combinator);
	    return cont(state, new PushLex(end, info), new Commasep(what, end), new PopLex());
	}
	
	class Block extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("}")) 
		    	return cont(state);
		    return pass(state, new Statement(), new Block());
		}
		
	}
	
	class Maybetype extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			if (isTS && type.equals(":")) 
				return cont(state, new Typedef());
			return false;
		}
		
	}
	
	class Typedef extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			if (type.equals("variable")) {
				state.marked = "variable-3"; 
				return cont(state);
			}
			return false;
		}
		
	}
	
	class Vardef extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			return pass(state, new PatternCombinator(), new Maybetype(), new MaybeAssign(), new VardefCont());
		}
		
	}
	
	class PatternCombinator extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("variable")) { 
		    	register(state, value); 
		    	return cont(state); 
		    }
		    if (type.equals("[")) return contCommasep(state, new PatternCombinator(), "]", "");
		    if (type.equals("{")) return contCommasep(state, new Proppattern(), "}", "");
		    return false;
		}
		
	}

	static Pattern PROP_PATTERN_PATTERN = Pattern.compile("^\\s*:");
	
	class Proppattern extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("variable") && stream.match(PROP_PATTERN_PATTERN, false).isEmpty()) {
		    	register(state, value);
		        return cont(state, new MaybeAssign());
		    }
		    if (type.equals("variable")) state.marked = "property";
		    return cont(state, new Expect(":"), new PatternCombinator(), new MaybeAssign());
		}
		
	}
	
	class MaybeAssign extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			if (value.equals("=")) return cont(state, new ExpressionNoComma());
			return false;
		}
		
	}
	
	class VardefCont extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			if (type.equals(",")) return cont(state, new Vardef());
			return false;
		}
		
	}
	
	class Maybeelse extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			if (type.equals("keyword b") && value.equals("else")) 
				return cont(state, new PushLex("form", "else"), new Statement(), new PopLex());
			return false;
		}
		
	}
	
	class Forspec extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			if (type.equals("(")) 
				return cont(state, new PushLex(")", ""), new Forspec1(), new Expect(")"), new PopLex());
			return false;
		}
		
	}
	
	class Forspec1 extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("var")) 
		    	return cont(state, new Vardef(), new Expect(";"), new Forspec2());
		    if (type.equals(";")) 
		    	return cont(state, new Forspec2());
		    if (type.equals("variable")) 
		    	return cont(state, new Formaybeinof());
		    return pass(state, new Expression(), new Expect(";"), new Forspec2());
		}
		
	}

	class Forspec2 extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals(";")) return cont(state, new Forspec3());
		    if (value.equals("in") || value.equals("of")) { 
		    	state.marked = "keyword"; 
		    	return cont(state, new Expression()); 
		    }
		    return pass(state, new Expression(), new Expect(";"), new Forspec3());
		}
		
	}
	
	class Forspec3 extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			if (!type.equals(")")) 
				cont(state, new Expression());
			return false;
		}
		
	}
	
	class Formaybeinof extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (value.equals("in") || value.equals("of")) { 
		    	state.marked = "keyword"; 
		    	return cont(state, new Expression()); 
		    }
		    return cont(state, new MaybeoperatorComma(), new Forspec2());
		}
		
	}
	
	class Functiondef extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (value.equals("*")) {
		    	state.marked = "keyword"; 
		    	return cont(state, new Functiondef());
		    }
		    if (type.equals("variable")) {
		    	register(state, value); 
		    	return cont(state, new Functiondef());
		    }
		    if (type.equals("(")) return cont(state, new PushContext(), new PushLex(")", ""), new Commasep(new Funarg(), ")"), new PopLex(), new Statement(), new PopContext());
		    return false;
		}
		
	}
	
	class Funarg extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("spread")) 
		    	return cont(state, new Funarg());
		    return pass(state, new PatternCombinator(), new Maybetype());
		}
		
	}
	
	class ClassName extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			if (type.equals("variable")) {
				register(state, value); 
				return cont(state, new ClassNameAfter());
			}
			return false;
		}
		
	}
	
	class ClassNameAfter extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (value.equals("extends")) 
		    	return cont(state, new Expression(), new ClassNameAfter());
		    if (type.equals("{")) 
		    	return cont(state, new PushLex("}", ""), new ClassBody(), new PopLex());
		    return false;
		}
		
	}
	
	class ClassBody extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("variable") || state.style.equals("keyword")) {
		    	state.marked = "property";
		        if (value.equals("get") || value.equals("set")) 
		        	return cont(state, new ClassGetterSetter(), new Functiondef(), new ClassBody());
		        return cont(state, new Functiondef(), new ClassBody());
		    }
		    if (value.equals("*")) {
		    	state.marked = "keyword";
		    	return cont(state, new ClassBody());
		    }
		    if (type.equals(";")) return cont(state, new ClassBody());
		    if (type.equals("}")) return cont(state);
		    return false;
		}
		
	}
	
	class ClassGetterSetter extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (!type.equals("variable")) 
		    	return pass(state);
		    state.marked = "property";
		    return cont(state);
		}
		
	}
	
	class AfterModule extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("string")) return cont(state, new Statement());
		    if (type.equals("variable")) { register(state, value); return cont(state, new MaybeFrom()); }
		    return false;
		}
		
	}
	
	class AfterExport extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (value.equals("*")) { 
		    	state.marked = "keyword"; 
		    	return cont(state, new MaybeFrom(), new Expect(";")); 
		    }
		    if (value.equals("default")) { 
		    	state.marked = "keyword"; 
		    	return cont(state, new Expression(), new Expect(";")); 
		    }
		    return pass(state, new Statement());
		}
		
	}
	
	class AfterImport extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("string")) return cont(state);
		    return pass(state, new ImportSpec(), new MaybeFrom());
		}
		
	}
	
	class ImportSpec extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("{")) return contCommasep(state, new ImportSpec(), "}", "");
		    if (type.equals("variable")) register(state, value);
		    return cont(state);
		}
		
	}
	
	class MaybeFrom extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
			if (value.equals("from")) { 
				state.marked = "keyword"; 
				return cont(state, new Expression()); 
			}
			return false;
		}
		
	}
	
	class ArrayLiteral extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("]")) return cont(state);
		    return pass(state, new ExpressionNoComma(), new MaybeArrayComprehension());
		}
		
	}
	
	class MaybeArrayComprehension extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("for")) return pass(state, new Comprehension(), new Expect("]"));
		    if (type.equals(",")) return cont(state, new Commasep(new MaybeexpressionNoComma(), "]"));
		    return pass(state, new Commasep(new ExpressionNoComma(), "]"));
		}
		
	}
	
	class Comprehension extends Combinator {

		@Override
		boolean combine(StringStream stream, State state, String type,
				String value) {
		    if (type.equals("for")) return cont(state, new Forspec(), new Comprehension());
		    if (type.equals("if")) return cont(state, new Expression(), new Comprehension());
		    return false;
		}
		
	}
	
	
	@Override
	protected State startState() {
		State state = new State();
		state.tokenize = new TokenBase();
		state.lastType = "sof";
		state.cc = new Stack<>();
		state.lexical = new JSLexical(-indentUnit(), 0, "block", false, null, "");
		state.indented = 0;
		state.localVars = null;
		state.context = null;
		return state;
	}

	@Override
	protected String token(StringStream stream, State state) {
		if (stream.sol()) {
			if (state.lexical.align == null)
				state.lexical.align = false;
			state.indented = stream.indentation();
	        findFatArrow(stream, state);
		}
	    if (!(state.tokenize instanceof TokenComment) && stream.eatSpace()) return "";
	    String style = state.tokenize.process(stream, state);
	    if (state.type.equals("comment")) return style;
	    state.lastType = state.type.equals("operator") && (state.content.equals("++") || state.content.equals("--")) ? "incdec" : state.type;
	    return parseJS(state, style, state.type, state.content, stream);
	}
	
	protected abstract boolean jsonldMode();
	
	protected abstract boolean json();
	
	protected Var globalVars() {
		return null;
	}
	
	protected boolean jsonMode() {
		return jsonldMode() || json();
	}
	
	public static class JavaScript extends JavascriptTokenizer {

		public JavaScript() {
			super(false);
		}

		@Override
		public boolean accept(String fileName) {
			return acceptExtensions(fileName, "js");
		}

		@Override
		protected boolean jsonldMode() {
			return false;
		}

		@Override
		protected boolean json() {
			return false;
		}
		
	}
	
	public static class TypeScript extends JavascriptTokenizer {

		public TypeScript() {
			super(true);
		}

		@Override
		public boolean accept(String fileName) {
			return acceptExtensions(fileName, "ts");
		}

		@Override
		protected boolean jsonldMode() {
			return false;
		}

		@Override
		protected boolean json() {
			return false;
		}
		
	}

	public static class JSON extends JavascriptTokenizer {

		public JSON() {
			super(false);
		}

		@Override
		public boolean accept(String fileName) {
			return acceptExtensions(fileName, "json", "map");
		}

		@Override
		protected boolean jsonldMode() {
			return false;
		}

		@Override
		protected boolean json() {
			return true;
		}
		
	}
	
	public static class JSON_LD extends JavascriptTokenizer {

		public JSON_LD() {
			super(false);
		}

		@Override
		public boolean accept(String fileName) {
			return acceptExtensions(fileName, "jsonld");
		}

		@Override
		protected boolean jsonldMode() {
			return true;
		}

		@Override
		protected boolean json() {
			return false;
		}
		
	}

}
