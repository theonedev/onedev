package com.pmease.commons.antlr.codeassist;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.apache.tika.io.IOUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.pmease.commons.antlr.ANTLRv4Lexer;
import com.pmease.commons.antlr.ANTLRv4Parser;
import com.pmease.commons.antlr.ANTLRv4Parser.AlternativeContext;
import com.pmease.commons.antlr.ANTLRv4Parser.AtomContext;
import com.pmease.commons.antlr.ANTLRv4Parser.BlockContext;
import com.pmease.commons.antlr.ANTLRv4Parser.EbnfContext;
import com.pmease.commons.antlr.ANTLRv4Parser.EbnfSuffixContext;
import com.pmease.commons.antlr.ANTLRv4Parser.ElementContext;
import com.pmease.commons.antlr.ANTLRv4Parser.GrammarSpecContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LabeledAltContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LabeledElementContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LabeledLexerElementContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LexerAltContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LexerAtomContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LexerBlockContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LexerElementContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LexerRuleBlockContext;
import com.pmease.commons.antlr.ANTLRv4Parser.LexerRuleSpecContext;
import com.pmease.commons.antlr.ANTLRv4Parser.ModeSpecContext;
import com.pmease.commons.antlr.ANTLRv4Parser.NotSetContext;
import com.pmease.commons.antlr.ANTLRv4Parser.ParserRuleSpecContext;
import com.pmease.commons.antlr.ANTLRv4Parser.RuleBlockContext;
import com.pmease.commons.antlr.ANTLRv4Parser.RuleSpecContext;
import com.pmease.commons.antlr.ANTLRv4Parser.SetElementContext;
import com.pmease.commons.antlr.AntlrUtils;
import com.pmease.commons.antlr.codeassist.ElementSpec.Multiplicity;
import com.pmease.commons.util.StringUtils;

public abstract class CodeAssist implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Class<? extends Lexer> lexerClass;
	
	private transient Constructor<? extends Lexer> lexerCtor;
	
	private final Map<String, RuleSpec> rules = new HashMap<>();
	
	private final Map<String, Integer> tokenTypesByLiteral = new HashMap<>();

	private final Map<String, Integer> tokenTypesByRule = new HashMap<>();
	
	public CodeAssist(Class<? extends Lexer> lexerClass) {
		this(lexerClass, new String[]{AntlrUtils.getDefaultGrammarFile(lexerClass)}, 
				AntlrUtils.getDefaultTokenFile(lexerClass));
	}

	/**
	 * Construct object representation of ANTLR grammar file.
	 * 
	 * @param grammarFiles
	 * 			grammar files in class path, relative to class path root
	 * @param tokenFile
	 * 			generated tokens file in class path, relative to class path root
	 */
	public CodeAssist(Class<? extends Lexer> lexerClass, String grammarFiles[], String tokenFile) {
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
			return new RuleSpec(this, name, alternatives);
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
		return new RuleSpec(this, name, alternatives);
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
		
		return new AlternativeSpec(this, null, elements);
	}
	
	private AlternativeSpec newAltenative(LabeledAltContext labeledAltContext) {
		String label;
		if (labeledAltContext.id() != null)
			label = labeledAltContext.id().getText();
		else
			label = null;
		
		return newAltenative(label, labeledAltContext.alternative());
	}
	
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
		
		return new AlternativeSpec(this, label, elements);
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
	
	private ElementSpec newElement(String label, AtomContext atomContext, EbnfSuffixContext ebnfSuffixContext) {
		Multiplicity multiplicity = newMultiplicity(ebnfSuffixContext);
		if (atomContext.terminal() != null) {
			if (atomContext.terminal().TOKEN_REF() != null) {
				String ruleName = atomContext.terminal().TOKEN_REF().getText();
				int tokenType = tokenTypesByRule.get(ruleName);
				if (tokenType != Token.EOF)
					return new LexerRuleRefElementSpec(this ,label, multiplicity, tokenType, ruleName);
				else
					return new EofElementSpec(this, label, multiplicity);
			} else {
				String literal = getLiteral(atomContext.terminal().STRING_LITERAL());
				int tokenType = tokenTypesByLiteral.get(literal);
				return new LiteralElementSpec(this, label, multiplicity, tokenType, literal);
			}
		} else if (atomContext.ruleref() != null) {
			return new RuleRefElementSpec(this, label, multiplicity, atomContext.ruleref().RULE_REF().getText());
		} else if (atomContext.notSet() != null) {
			return new NotTokenElementSpec(this, label, multiplicity, getNegativeTokenTypes(atomContext.notSet()));
		} else if (atomContext.DOT() != null) {
			return new AnyTokenElementSpec(this, label, multiplicity);
		} else {
			throw new IllegalStateException();
		}
	}

	private String getLiteral(TerminalNode terminal) {
		String literal = terminal.getText();
		return literal.substring(1, literal.length()-1);
	}
	
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
					return new EofElementSpec(this, label, multiplicity);
			} else {
				String literal = getLiteral(lexerAtomContext.terminal().STRING_LITERAL());
				Integer tokenType = tokenTypesByLiteral.get(literal);
				if (tokenType == null)
					tokenType = 0;
				return new LiteralElementSpec(this, label, multiplicity, tokenType, literal);
			}
		} else if (lexerAtomContext.RULE_REF() != null) {
			return new RuleRefElementSpec(this, label, multiplicity, lexerAtomContext.RULE_REF().getText());
		} else if (lexerAtomContext.notSet() != null 
				|| lexerAtomContext.DOT() != null 
				|| lexerAtomContext.LEXER_CHAR_SET()!=null 
				|| lexerAtomContext.range() != null) {
			return new AnyTokenElementSpec(this, label, multiplicity);
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
		RuleSpec rule = new RuleSpec(this, ruleName, alternatives);
		rules.put(ruleName, rule);
		return new RuleRefElementSpec(this, label, newMultiplicity(ebnfSuffixContext), ruleName);
	}
	
	private ElementSpec newElement(@Nullable String label, LexerBlockContext lexerBlockContext, @Nullable EbnfSuffixContext ebnfSuffixContext) {
		List<AlternativeSpec> alternatives = new ArrayList<>();
		for (LexerAltContext lexerAltContext: lexerBlockContext.lexerAltList().lexerAlt())
			alternatives.add(newAltenative(lexerAltContext));
		String ruleName = UUID.randomUUID().toString();
		RuleSpec rule = new RuleSpec(this, ruleName, alternatives);
		rules.put(ruleName, rule);
		return new RuleRefElementSpec(this, label, newMultiplicity(ebnfSuffixContext), ruleName);
	}
	
	private ElementSpec newElement(EbnfContext ebnfContext) {
		if (ebnfContext.blockSuffix() != null)
			return newElement(null, ebnfContext.block(), ebnfContext.blockSuffix().ebnfSuffix());
		else
			return newElement(null, ebnfContext.block(), null);
	}
	
	@Nullable
	public RuleSpec getRule(String ruleName) {
		return rules.get(ruleName);
	}
	
	public final AssistStream lex(String content) {
		try {
			List<Token> tokens = new ArrayList<>();
			Lexer lexer = getLexerCtor().newInstance(new ANTLRInputStream(content));
			lexer.removeErrorListeners();
			Token token;
			do {
				token = lexer.nextToken();
				if (token.getChannel() == Token.DEFAULT_CHANNEL)
					tokens.add(token);
			} while (token.getType() != Token.EOF);
			
			return new AssistStream(tokens);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<InputSuggestion> suggest(InputStatus inputStatus, String ruleName) {
		RuleSpec rule = Preconditions.checkNotNull(getRule(ruleName));
		
		AssistStream stream = lex(inputStatus.getContentBeforeCaret());
		
		String inputContent = inputStatus.getContent();
		List<InputSuggestion> inputSuggestions = new ArrayList<>();
		Map<String, List<ElementReplacement>> grouped = new LinkedHashMap<>();
		for (ElementReplacement replacement: suggest(rule, stream, inputStatus)) {
			String key = inputContent.substring(0, replacement.begin) 
					+ replacement.content + inputContent.substring(replacement.end);
			List<ElementReplacement> value = grouped.get(key);
			if (value == null) {
				value = new ArrayList<>();
				grouped.put(key, value);
			}
			value.add(replacement);
		}
		
		for (Map.Entry<String, List<ElementReplacement>> entry: grouped.entrySet())	 {
			List<ElementReplacement> value = entry.getValue();
			ElementReplacement replacement = value.get(0);
			String label = replacement.label;
			String description = replacement.description;
			String content = entry.getKey(); 
			if (replacement.end <= inputStatus.getCaret()) {
				List<String> contents = new ArrayList<>();
				for (ElementReplacement each: value) {
					content = inputContent.substring(0, each.begin) + each.content;
					for (String mandatory: getMandatoriesAfter(each.node)) {
						String prevContent = content;
						content += mandatory;
						if (tokenTypesByLiteral.containsKey(mandatory)) {
							stream = lex(content);
							if (!stream.isEof()) {
								Token lastToken = stream.getToken(stream.size()-2);
								if (lastToken.getStartIndex() != content.length() - mandatory.length()
										|| lastToken.getStopIndex() != content.length()-1) {
									content = prevContent + " " + mandatory;
								}
							} else {
								content = prevContent + " " + mandatory;
							}
						}
					}
					content += inputContent.substring(each.end);
					
					if (contents.isEmpty()) {
						contents.add(content);
					} else if (!contents.get(contents.size()-1).equals(content)) {
						content = entry.getKey();
						break;
					}
				}
			} 

			/*
			 * Adjust caret to move it to next place expecting user input, that is, we move 
			 * it after all mandatory tokens after the replacement, unless user puts caret 
			 * explicitly in the middle of replacement via suggestion, indicating the 
			 * replacement has some place holders expecting user input. 
			 */
			int caret;
			if (replacement.caret == replacement.content.length()) {
				// caret is not at the middle of replacement, so we need to move it to 
				// be after mandatory tokens
				caret = content.length() - inputContent.length() + replacement.end; 
				if (content.equals(entry.getKey())) { 
					// in case mandatory tokens are not added after replacement, 
					// we skip existing mandatory tokens in current input
					String contentAfterCaret = content.substring(caret);
					caret += skipMandatories(contentAfterCaret, getMandatoriesAfter(replacement.node));
				}
			} else {
				caret = replacement.begin + replacement.content.length();
			}
			
			inputSuggestions.add(new InputSuggestion(content, caret, label, description));
		}
		
		Set<String> suggestedContents = Sets.newHashSet(inputStatus.getContent());
		for (Iterator<InputSuggestion> it = inputSuggestions.iterator(); it.hasNext();) {
			String content = it.next().getContent();
			if (suggestedContents.contains(content))
				it.remove();
			else
				suggestedContents.add(content);
		}
		return inputSuggestions;
	}
	
	private int skipMandatories(String content, List<String> mandatories) {
		String mandatory = StringUtils.join(mandatories, "");
		mandatory = StringUtils.deleteWhitespace(mandatory);
		if (mandatory.length() != 0 && content.length() != 0) {
			int mandatoryIndex = 0;
			int contentIndex = 0;
			while (true) {
				char contentChar = content.charAt(contentIndex);
				if (!Character.isWhitespace(contentChar)) {
					if (contentChar != mandatory.charAt(mandatoryIndex))
						break;
					mandatoryIndex++;
				} 
				contentIndex++;
				if (mandatoryIndex == mandatory.length() || contentIndex == content.length())
					break;
			}
			if (mandatoryIndex == mandatory.length())
				return contentIndex;
			else
				return 0;
		} else {
			return 0;
		}
	}
	
	private List<ElementSuggestion> suggest(InputStatus inputStatus, List<TokenNode> matches) {
		List<ElementSuggestion> suggestions = new ArrayList<>();
		for (TokenNode match: matches) {
			String matchWith = inputStatus.getContent().substring(
					match.getToken().getStopIndex()+1, inputStatus.getCaret());
			matchWith = StringUtils.trimStart(matchWith);
			ElementSpec matchSpec = (ElementSpec) match.getSpec();
			suggestions.addAll(matchSpec.suggestNext(new ParseTree(match), match.getParent(), matchWith));
		}
		return suggestions;
	}
	
	private List<ElementReplacement> suggest(RuleSpec spec, AssistStream stream, InputStatus inputStatus) {
		List<ElementReplacement> elementReplacements = new ArrayList<>();
		
		List<ElementSuggestion> elementSuggestions = new ArrayList<>();
		if (stream.isEof()) {
			String matchWith = StringUtils.trimStart(inputStatus.getContentBeforeCaret());
			elementSuggestions.addAll(spec.suggestFirst(null, null, matchWith, new HashSet<String>()));
		} else {
			SpecMatch match = spec.match(stream, null, null, new HashMap<String, Integer>());
			if (match.getPaths().isEmpty()) {
				String matchWith = StringUtils.trimStart(inputStatus.getContentBeforeCaret());
				elementSuggestions.addAll(spec.suggestFirst(null, null, matchWith, new HashSet<String>()));
			} else {
				elementSuggestions.addAll(suggest(inputStatus, match.getPaths()));
				int index = stream.getTokenIndex(match.getPaths().get(0).getToken());
				if (index>0) {
					List<Token> tokens = new ArrayList<>();
					for (int i=0; i<index; i++)
						tokens.add(stream.getToken(i));
					tokens.add(stream.getToken(stream.size()-1));
					match = spec.match(new AssistStream(tokens), null, null, new HashMap<String, Integer>());
					elementSuggestions.addAll(suggest(inputStatus, match.getPaths()));
				}
			}
		}

		String inputContent = inputStatus.getContent();
		for (ElementSuggestion elementSuggestion: elementSuggestions) {
			int replaceStart = inputStatus.getCaret() - elementSuggestion.getMatchWith().length();
			int replaceEnd = inputStatus.getCaret();
			String contentAfterReplaceStart = inputContent.substring(replaceStart);
			AssistStream streamAfterReplaceStart = lex(contentAfterReplaceStart);
			if (!streamAfterReplaceStart.isEof() && streamAfterReplaceStart.getToken(0).getStartIndex() == 0) {
				ElementSpec elementSpec = (ElementSpec) elementSuggestion.getNode().getSpec();
				if (elementSpec.matchesOnce(streamAfterReplaceStart)) {
					if (streamAfterReplaceStart.getIndex() != 0) {
						int lastTokenIndex = streamAfterReplaceStart.getPreviousToken().getStopIndex()+1;
						if (replaceStart + lastTokenIndex > replaceEnd)
							replaceEnd = replaceStart + lastTokenIndex;
					}
				}
			}

			String before = inputContent.substring(0, replaceStart);

			for (InputSuggestion inputSuggestion: elementSuggestion.getInputSuggestions()) {
				ElementReplacement elementReplacement = new ElementReplacement();
				elementReplacement.node = elementSuggestion.getNode();
				elementReplacement.begin = replaceStart;
				elementReplacement.end = replaceEnd;
				elementReplacement.label = inputSuggestion.getLabel();
				elementReplacement.description = inputSuggestion.getDescription();
				
				elementReplacement.content = inputSuggestion.getContent();
				elementReplacement.caret = inputSuggestion.getCaret();
				if (elementSuggestion.getParseTree() != null) { 
					AssistStream newStream = lex(before + elementReplacement.content);
					Token lastToken = elementSuggestion.getParseTree().getLastNode().getToken();
					int index = stream.getTokenIndex(lastToken);
					if (newStream.size() > index+1) {
						Token newToken = newStream.getToken(index);
						if (lastToken.getStartIndex() != newToken.getStartIndex()
								|| lastToken.getStopIndex() != newToken.getStopIndex()) {
							continue;
						}
					} else {
						continue;
					}
				} 
				
				elementReplacements.add(elementReplacement);
			}
		}
		return elementReplacements;
	}
	
	private List<String> getMandatoriesAfter(Node elementNode) {
		List<String> literals = new ArrayList<>();
		ElementSpec elementSpec = (ElementSpec) elementNode.getSpec();
		if (elementSpec.getMultiplicity() == Multiplicity.ONE 
				|| elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_ONE) {
			AlternativeSpec alternativeSpec = (AlternativeSpec) elementNode.getParent().getSpec();
			int specIndex = alternativeSpec.getElements().indexOf(elementSpec);
			if (specIndex == alternativeSpec.getElements().size()-1) {
				elementNode = elementNode.getParent().getParent().getParent();
				if (elementNode != null)
					return getMandatoriesAfter(elementNode);
			} else {
				elementSpec = alternativeSpec.getElements().get(specIndex+1);
				if (elementSpec.getMultiplicity() == Multiplicity.ONE
						|| elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
					MandatoryScan scan = elementSpec.scanMandatories(new HashSet<String>());
					literals = scan.getMandatories();
					if (!scan.isStop()) {
						elementNode = new Node(elementSpec, elementNode.getParent(), null);
						literals.addAll(getMandatoriesAfter(elementNode));
					}
				}
			}
		} 
		return literals;
	}
	
	protected abstract List<InputSuggestion> suggest(@Nullable ParseTree parseTree, 
			Node elementNode, String matchWith);

}
