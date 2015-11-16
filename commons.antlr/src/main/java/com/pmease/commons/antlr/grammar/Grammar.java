package com.pmease.commons.antlr.grammar;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

import com.google.common.base.Preconditions;
import com.pmease.commons.antlr.ANTLRv4Lexer;
import com.pmease.commons.antlr.ANTLRv4Parser;
import com.pmease.commons.antlr.ANTLRv4Parser.AtomContext;
import com.pmease.commons.antlr.ANTLRv4Parser.BlockContext;
import com.pmease.commons.antlr.ANTLRv4Parser.EbnfContext;
import com.pmease.commons.antlr.ANTLRv4Parser.EbnfSuffixContext;
import com.pmease.commons.antlr.ANTLRv4Parser.ElementContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LabeledAltContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LabeledElementContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LexerRuleSpecContext;
import com.pmease.commons.antlr.ANTLRv4Parser.NotSetContext;
import com.pmease.commons.antlr.ANTLRv4Parser.ParserRuleSpecContext;
import com.pmease.commons.antlr.ANTLRv4Parser.RuleBlockContext;
import com.pmease.commons.antlr.ANTLRv4Parser.RuleSpecContext;
import com.pmease.commons.antlr.ANTLRv4Parser.SetElementContext;
import com.pmease.commons.antlr.ANTLRv4Parser.TerminalContext;
import com.pmease.commons.antlr.grammar.Element.Multiplicity;

public class Grammar implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String PARSER_SUFFIX = "parser";

	private final Map<String, Rule> rules = new HashMap<>();
	
	private final Map<String, Integer> tokenTypesByValue = new HashMap<>();

	private final Map<String, Integer> tokenTypesByName = new HashMap<>();
	
	public Grammar(Class<? extends Parser> parserClass, String entryRule) {
		String parserName = parserClass.getSimpleName();
		Preconditions.checkArgument(parserName.endsWith(PARSER_SUFFIX));
		String grammarName = parserName.substring(0, parserName.length() - PARSER_SUFFIX.length());
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(grammarName + ".tokens")) {
			Properties props = new Properties();
			props.load(is);
			for (Map.Entry<Object, Object> entry: props.entrySet()) {
				String key = (String) entry.getKey();
				Integer value = Integer.valueOf((String) entry.getValue());
				if (key.startsWith("'"))
					tokenTypesByValue.put(key.substring(1, key.length()-1), value);
				else
					tokenTypesByName.put(key, value);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try (InputStream is = parserClass.getResourceAsStream(grammarName + ".g4")) {
			ANTLRv4Lexer lexer = new ANTLRv4Lexer(new ANTLRInputStream(is));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ANTLRv4Parser parser = new ANTLRv4Parser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			for (RuleSpecContext ruleSpecContext: parser.rules().ruleSpec()) {
				Rule rule = newRule(ruleSpecContext);
				rules.put(rule.getName(), rule);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Rule newRule(RuleSpecContext ruleSpecContext) {
		String name;
		List<Altenative> altenatives = new ArrayList<>();
		ParserRuleSpecContext parserRuleSpecContext = ruleSpecContext.parserRuleSpec();
		if (parserRuleSpecContext != null) {
			name = parserRuleSpecContext.RULE_REF().getText();
			RuleBlockContext ruleBlock = parserRuleSpecContext.ruleBlock();
			for (LabeledAltContext labeledAlt: ruleBlock.ruleAltList().labeledAlt())
				altenatives.add(newAltenative(labeledAlt));
		} else {
			LexerRuleSpecContext lexerRuleSpecContext = ruleSpecContext.lexerRuleSpec();
			name = lexerRuleSpecContext.TOKEN_REF().getText();
			
		}
		return new Rule(this, name, altenatives);
	}
	
	private Altenative newAltenative(LabeledAltContext labeledAltContext) {
		String label;
		List<Element> elements = new ArrayList<>();
		if (labeledAltContext.id() != null)
			label = labeledAltContext.id().getText();
		else
			label = null;
		
		for (ElementContext elementContext: labeledAltContext.alternative().element())
			elements.add(newElement(elementContext));
		
		return new Altenative(this, label, elements);
	}
	
	private Element newElement(ElementContext elementContext) {
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
		}
	}
	
	private int getTokenType(TerminalContext terminalContext) {
		if (terminalContext.STRING_LITERAL() != null)
			return tokenTypesByValue.get(terminalContext.STRING_LITERAL().getText());
		else
			return tokenTypesByName.get(terminalContext.TOKEN_REF().getText());
	}
	
	private Element newElement(String label, AtomContext atomContext, EbnfSuffixContext ebnfSuffixContext) {
		Multiplicity multiplicity = newMultiplicity(ebnfSuffixContext);
		if (atomContext.terminal() != null) {
			String tokenName;
			if (atomContext.terminal().TOKEN_REF() != null)
				tokenName = atomContext.terminal().TOKEN_REF().getText();
			else
				tokenName = null;
			return new TokenElement(this, label, multiplicity, getTokenType(atomContext.terminal()), tokenName);
		} else if (atomContext.ruleref() != null) {
			return new RuleElement(this, label, multiplicity, atomContext.ruleref().RULE_REF().getText());
		} else if (atomContext.notSet() != null) {
			return new NegativeTokensElement(this, label, multiplicity, getNegativeTokenTypes(atomContext.notSet()));
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
		
	}
	
	private Multiplicity newMultiplicity(EbnfSuffixContext ebnfSuffixContext) {
		if (ebnfSuffixContext != null) {
			if (ebnfSuffixContext.STAR() != null)
				return Multiplicity.ZERO_OR_MORE;
			else if (ebnfSuffixContext.PLUS() != null)
				return Multiplicity.ONE_OR_MORE;
			else
				return Multiplicity.ONE_OR_ZERO;
		} else {
			return Multiplicity.ONE;
		}
	}
	
	private Element newElement(String label, BlockContext blockContext, EbnfSuffixContext ebnfSuffixContext) {
		
	}
	
	private Element newElement(EbnfContext ebnfContext) {
		
	}
	
	public Map<String, Rule> getRules() {
		return rules;
	}

}
