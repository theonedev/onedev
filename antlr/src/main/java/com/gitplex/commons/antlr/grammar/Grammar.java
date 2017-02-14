package com.gitplex.commons.antlr.grammar;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.IOUtils;

import com.gitplex.commons.antlr.ANTLRv4Lexer;
import com.gitplex.commons.antlr.ANTLRv4Parser;
import com.gitplex.commons.antlr.AntlrUtils;
import com.gitplex.commons.antlr.ANTLRv4Parser.AlternativeContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.AtomContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.BlockContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.EbnfContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.EbnfSuffixContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.ElementContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.GrammarSpecContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.LabeledAltContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.LabeledElementContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.LabeledLexerElementContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.LexerAltContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.LexerAtomContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.LexerBlockContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.LexerElementContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.LexerRuleBlockContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.LexerRuleSpecContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.ModeSpecContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.NotSetContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.ParserRuleSpecContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.RuleBlockContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.RuleSpecContext;
import com.gitplex.commons.antlr.ANTLRv4Parser.SetElementContext;
import com.gitplex.commons.antlr.grammar.ElementSpec.Multiplicity;
import com.gitplex.commons.util.StringUtils;

public class Grammar implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Class<? extends Lexer> lexerClass;
	
	private transient Constructor<? extends Lexer> lexerCtor;
	
	private final Map<String, RuleSpec> rules = new HashMap<>();
	
	private final Map<String, Integer> tokenTypesByLiteral = new HashMap<>();

	private final Map<String, Integer> tokenTypesByRule = new HashMap<>();
	
	private final Set<String> blockRuleNames = new HashSet<>();
	
	/**
	 * Code assist constructor
	 * @param lexerClass
	 * 			lexer class to be used to lex code. Other required information such as 
	 * 			grammar files and token file will be derived from the lexer class
	 */
	public Grammar(Class<? extends Lexer> lexerClass) {
		this(lexerClass, new String[]{AntlrUtils.getDefaultGrammarFile(lexerClass)}, 
				AntlrUtils.getDefaultTokenFile(lexerClass));
	}

	/**
	 * Code assist constructor.
	 * 
	 * @param lexerClass
	 * 			lexer class to be used to lex code
	 * @param grammarFiles
	 * 			grammar files in class path, relative to class path root
	 * @param tokenFile
	 * 			generated tokens file in class path, relative to class path root
	 */
	public Grammar(Class<? extends Lexer> lexerClass, String grammarFiles[], String tokenFile) {
		this.lexerClass = lexerClass;
		tokenTypesByRule.put("EOF", Token.EOF);
		
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(tokenFile)) {
			for (String line: IOUtils.readLines(is)) {
				String key = StringUtils.substringBeforeLast(line, "=");
				Integer value = Integer.valueOf(StringUtils.substringAfterLast(line, "="));
				if (key.startsWith("'"))
					tokenTypesByLiteral.put(key.substring(1, key.length()-1), value);
				else
					tokenTypesByRule.put(key, value);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	
		for (String grammarFile: grammarFiles) {
			try (InputStream is = getClass().getClassLoader().getResourceAsStream(grammarFile)) {
				ANTLRv4Lexer lexer = new ANTLRv4Lexer(new ANTLRInputStream(is));
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				ANTLRv4Parser parser = new ANTLRv4Parser(tokens);
				parser.removeErrorListeners();
				parser.setErrorHandler(new BailErrorStrategy());
				GrammarSpecContext grammarSpecContext = parser.grammarSpec();
				for (RuleSpecContext ruleSpecContext: grammarSpecContext.rules().ruleSpec()) {
					RuleSpec rule = newRule(ruleSpecContext);
					rules.put(rule.getName(), rule);
				}
				for (ModeSpecContext modeSpecContext: grammarSpecContext.modeSpec()) {
					for (LexerRuleSpecContext lexerRuleSpecContext: modeSpecContext.lexerRuleSpec()) {
						RuleSpec rule = newRule(lexerRuleSpecContext);
						rules.put(rule.getName(), rule);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		// initialize rule properties to avoid changing grammar after 
		// the constructor
		for (RuleSpec rule: rules.values()) {
			rule.isAllowEmpty();
			rule.getPossiblePrefixes();
			rule.scanMandatories();
		}
	}
	
	private Constructor<? extends Lexer> getLexerCtor() {
		if (lexerCtor == null) {
			try {
				lexerCtor = lexerClass.getConstructor(CharStream.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		} 
		return lexerCtor;
	}
	
	private RuleSpec newRule(RuleSpecContext ruleSpecContext) {
		ParserRuleSpecContext parserRuleSpecContext = ruleSpecContext.parserRuleSpec();
		if (parserRuleSpecContext != null) {
			String name = parserRuleSpecContext.RULE_REF().getText();
			List<AlternativeSpec> alternatives = new ArrayList<>();
			RuleBlockContext ruleBlockContext = parserRuleSpecContext.ruleBlock();
			for (LabeledAltContext labeledAltContext: ruleBlockContext.ruleAltList().labeledAlt())
				alternatives.add(newAltenative(labeledAltContext));
			return new RuleSpec(name, alternatives);
		} else {
			return newRule(ruleSpecContext.lexerRuleSpec());
		}
	}
	
	private RuleSpec newRule(LexerRuleSpecContext lexerRuleSpecContext) {
		String name;
		List<AlternativeSpec> alternatives = new ArrayList<>();
		name = lexerRuleSpecContext.TOKEN_REF().getText();
		LexerRuleBlockContext lexerRuleBlockContext = lexerRuleSpecContext.lexerRuleBlock();
		for (LexerAltContext lexerAltContext: lexerRuleBlockContext.lexerAltList().lexerAlt())
			alternatives.add(newAltenative(lexerAltContext));
		return new RuleSpec(name, alternatives);
	}
	
	private AlternativeSpec newAltenative(LexerAltContext lexerAltContext) {
		List<ElementSpec> elements = new ArrayList<>();
		if (lexerAltContext.lexerElements() != null) {
			for (LexerElementContext lexerElementContext: lexerAltContext.lexerElements().lexerElement()) {
				ElementSpec element = newElement(lexerElementContext);
				if (element != null)
					elements.add(element);
			}
		}
		
		return new AlternativeSpec(null, elements);
	}
	
	private AlternativeSpec newAltenative(LabeledAltContext labeledAltContext) {
		String label;
		if (labeledAltContext.id() != null)
			label = labeledAltContext.id().getText();
		else
			label = null;
		
		return newAltenative(label, labeledAltContext.alternative());
	}
	
	@Nullable
	private ElementSpec newElement(LexerElementContext lexerElementContext) {
		LabeledLexerElementContext labeledLexerElementContext = lexerElementContext.labeledLexerElement();
		if (labeledLexerElementContext != null) {
			String label = labeledLexerElementContext.id().getText();
			LexerAtomContext lexerAtomContext = labeledLexerElementContext.lexerAtom();
			if (lexerAtomContext != null)
				return newElement(label, lexerAtomContext, lexerElementContext.ebnfSuffix());
			else 
				return newElement(label, labeledLexerElementContext.block(), lexerElementContext.ebnfSuffix());
		} else if (lexerElementContext.lexerAtom() != null) {
			return newElement(null, lexerElementContext.lexerAtom(), lexerElementContext.ebnfSuffix());
		} else if (lexerElementContext.lexerBlock() != null) {
			return newElement(null, lexerElementContext.lexerBlock(), lexerElementContext.ebnfSuffix());
		} else {
			return null;
		}
	}
	
	private AlternativeSpec newAltenative(@Nullable String label, AlternativeContext alternativeContext) {
		List<ElementSpec> elements = new ArrayList<>();
		for (ElementContext elementContext: alternativeContext.element()) {
			ElementSpec element = newElement(elementContext);
			if (element != null)
				elements.add(element);
		}
		
		return new AlternativeSpec(label, elements);
	}
	
	@Nullable
	private ElementSpec newElement(ElementContext elementContext) {
		LabeledElementContext labeledElementContext = elementContext.labeledElement();
		if (labeledElementContext != null) {
			String label = labeledElementContext.id().getText();
			AtomContext atomContext = labeledElementContext.atom();
			if (atomContext != null)
				return newElement(label, atomContext, elementContext.ebnfSuffix());
			else 
				return newElement(label, labeledElementContext.block(), elementContext.ebnfSuffix());
		} else if (elementContext.atom() != null) {
			return newElement(null, elementContext.atom(), elementContext.ebnfSuffix());
		} else if (elementContext.ebnf() != null) {
			return newElement(elementContext.ebnf());
		} else {
			return null;
		}
	}
	
	@Nullable
	private ElementSpec newElement(String label, AtomContext atomContext, EbnfSuffixContext ebnfSuffixContext) {
		Multiplicity multiplicity = newMultiplicity(ebnfSuffixContext);
		if (atomContext.terminal() != null) {
			if (atomContext.terminal().TOKEN_REF() != null) {
				String ruleName = atomContext.terminal().TOKEN_REF().getText();
				int tokenType = tokenTypesByRule.get(ruleName);
				if (tokenType != Token.EOF)
					return new LexerRuleRefElementSpec(this ,label, multiplicity, tokenType, ruleName);
				else
					return null;
			} else {
				String literal = getLiteral(atomContext.terminal().STRING_LITERAL());
				int tokenType = tokenTypesByLiteral.get(literal);
				return new LiteralElementSpec(label, multiplicity, tokenType, literal);
			}
		} else if (atomContext.ruleref() != null) {
			return new RuleRefElementSpec(this, label, multiplicity, atomContext.ruleref().RULE_REF().getText());
		} else if (atomContext.notSet() != null) {
			return new NotTokenElementSpec(this, label, multiplicity, getNegativeTokenTypes(atomContext.notSet()));
		} else if (atomContext.DOT() != null) {
			return new AnyTokenElementSpec(label, multiplicity);
		} else {
			throw new IllegalStateException();
		}
	}

	private String getLiteral(TerminalNode terminal) {
		String literal = terminal.getText();
		return literal.substring(1, literal.length()-1);
	}
	
	@Nullable
	private ElementSpec newElement(String label, LexerAtomContext lexerAtomContext, EbnfSuffixContext ebnfSuffixContext) {
		Multiplicity multiplicity = newMultiplicity(ebnfSuffixContext);
		if (lexerAtomContext.terminal() != null) {
			if (lexerAtomContext.terminal().TOKEN_REF() != null) {
				String ruleName = lexerAtomContext.terminal().TOKEN_REF().getText();
				Integer tokenType = tokenTypesByRule.get(ruleName);
				if (tokenType == null) // fragment rule
					tokenType = 0;
				if (tokenType != Token.EOF)
					return new LexerRuleRefElementSpec(this, label, multiplicity, tokenType, ruleName);
				else
					return null;
			} else {
				String literal = getLiteral(lexerAtomContext.terminal().STRING_LITERAL());
				Integer tokenType = tokenTypesByLiteral.get(literal);
				if (tokenType == null)
					tokenType = 0;
				return new LiteralElementSpec(label, multiplicity, tokenType, literal);
			}
		} else if (lexerAtomContext.RULE_REF() != null) {
			return new RuleRefElementSpec(this, label, multiplicity, lexerAtomContext.RULE_REF().getText());
		} else if (lexerAtomContext.notSet() != null 
				|| lexerAtomContext.DOT() != null 
				|| lexerAtomContext.LEXER_CHAR_SET()!=null 
				|| lexerAtomContext.range() != null) {
			// Use AnyTokenElementSpec here to as it does not affect our code assist analysis
			return new AnyTokenElementSpec(label, multiplicity);
		} else {
			throw new IllegalStateException();
		}
	}
	
	private Set<Integer> getNegativeTokenTypes(NotSetContext notSetContext) {
		Set<Integer> negativeTokenTypes = new HashSet<>();
		if (notSetContext.setElement() != null) {
			negativeTokenTypes.add(getTokenType(notSetContext.setElement()));
		} else {
			for (SetElementContext setElementContext: notSetContext.blockSet().setElement())
				negativeTokenTypes.add(getTokenType(setElementContext));
		}
		return negativeTokenTypes;
	}
	
	private int getTokenType(SetElementContext setElementContext) {
		Integer tokenType;
		if (setElementContext.STRING_LITERAL() != null) 
			tokenType = tokenTypesByLiteral.get(getLiteral(setElementContext.STRING_LITERAL()));
		else if (setElementContext.TOKEN_REF() != null)
			tokenType = tokenTypesByRule.get(setElementContext.TOKEN_REF().getText());
		else 
			tokenType = null;
		if (tokenType != null)
			return tokenType;
		else
			throw new IllegalStateException();
	}
	
	private Multiplicity newMultiplicity(@Nullable EbnfSuffixContext ebnfSuffixContext) {
		if (ebnfSuffixContext != null) {
			if (ebnfSuffixContext.STAR() != null)
				return Multiplicity.ZERO_OR_MORE;
			else if (ebnfSuffixContext.PLUS() != null)
				return Multiplicity.ONE_OR_MORE;
			else
				return Multiplicity.ZERO_OR_ONE;
		} else {
			return Multiplicity.ONE;
		}
	}
	
	private ElementSpec newElement(@Nullable String label, BlockContext blockContext, @Nullable EbnfSuffixContext ebnfSuffixContext) {
		List<AlternativeSpec> alternatives = new ArrayList<>();
		for (AlternativeContext alternativeContext: blockContext.altList().alternative())
			alternatives.add(newAltenative(null, alternativeContext));
		String ruleName = UUID.randomUUID().toString();
		blockRuleNames.add(ruleName);
		RuleSpec rule = new RuleSpec(ruleName, alternatives);
		rules.put(ruleName, rule);
		return new RuleRefElementSpec(this, label, newMultiplicity(ebnfSuffixContext), ruleName);
	}
	
	private ElementSpec newElement(@Nullable String label, LexerBlockContext lexerBlockContext, @Nullable EbnfSuffixContext ebnfSuffixContext) {
		List<AlternativeSpec> alternatives = new ArrayList<>();
		for (LexerAltContext lexerAltContext: lexerBlockContext.lexerAltList().lexerAlt())
			alternatives.add(newAltenative(lexerAltContext));
		String ruleName = UUID.randomUUID().toString();
		blockRuleNames.add(ruleName);
		RuleSpec rule = new RuleSpec(ruleName, alternatives);
		rules.put(ruleName, rule);
		return new RuleRefElementSpec(this, label, newMultiplicity(ebnfSuffixContext), ruleName);
	}
	
	private ElementSpec newElement(EbnfContext ebnfContext) {
		if (ebnfContext.blockSuffix() != null)
			return newElement(null, ebnfContext.block(), ebnfContext.blockSuffix().ebnfSuffix());
		else
			return newElement(null, ebnfContext.block(), null);
	}
	
	public boolean isBlockRule(String ruleName) {
		return blockRuleNames.contains(ruleName);
	}
	
	@Nullable
	public String getTokenNameByType(int tokenType) {
		for (Map.Entry<String, Integer> entry: tokenTypesByRule.entrySet()) {
			if (entry.getValue() == tokenType)
				return entry.getKey();
		}
		return null;
	}
	
	@Nullable
	public Integer getTokenTypeByLiteral(String literal) {
		return tokenTypesByLiteral.get(literal);
	}
	
	@Nullable
	public RuleSpec getRule(String ruleName) {
		return rules.get(ruleName);
	}
	
	public final List<Token> lex(String content) {
		try {
			List<Token> tokens = new ArrayList<>();
			Lexer lexer = getLexerCtor().newInstance(new ANTLRInputStream(content));
			lexer.removeErrorListeners();
			Token token = lexer.nextToken();
			while (token.getType() != Token.EOF) {
				if (token.getChannel() == Token.DEFAULT_CHANNEL)
					tokens.add(token);
				token = lexer.nextToken();
			}
			
			return tokens;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
