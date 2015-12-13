package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.pmease.commons.antlr.Grammar;
import com.pmease.commons.antlr.grammar.AlternativeSpec;
import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.RuleRefElementSpec;
import com.pmease.commons.antlr.grammar.RuleSpec;
import com.pmease.commons.antlr.grammar.TerminalElementSpec;
import com.pmease.commons.antlr.grammar.ElementSpec.Multiplicity;
import com.pmease.commons.antlr.parser.EarleyParser;
import com.pmease.commons.antlr.parser.Element;
import com.pmease.commons.antlr.parser.Node;
import com.pmease.commons.antlr.parser.State;
import com.pmease.commons.util.StringUtils;

public abstract class CodeAssist implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Grammar grammar;
	
	public CodeAssist(Grammar grammar) {
		this.grammar = grammar;
	}

	/**
	 * Code assist constructor
	 * @param lexerClass
	 * 			lexer class to be used to lex code. Other required information such as 
	 * 			grammar files and token file will be derived from the lexer class
	 */
	public CodeAssist(Class<? extends Lexer> lexerClass) {
		this(new Grammar(lexerClass));
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
	public CodeAssist(Class<? extends Lexer> lexerClass, String grammarFiles[], String tokenFile) {
		this(new Grammar(lexerClass, grammarFiles, tokenFile));
	}
	
	public List<InputCompletion> suggest(InputStatus inputStatus, String ruleName) {
		RuleSpec rule = Preconditions.checkNotNull(grammar.getRule(ruleName));
		
		String inputContent = inputStatus.getContent();
		List<InputCompletion> inputSuggestions = new ArrayList<>();
		
		/*
		 * Mandatory literals may come after a suggestion (for instance if a method name is suggested, 
		 * the character '(' can be a mandatory literal), so for every suggestion, we need to 
		 * append mandatory literals to make user typing less, however if a suggested text has two or
		 * more different mandatories (this is possible if the suggested text comes from different 
		 * element specs, and each spec calculates a different set of mandatories after the text), we 
		 * should display the suggested text as a single entry in the final suggestion list without 
		 * appending mandatories they are no longer mandatories from aspect of whole rule. Below code
		 * mainly handles this logic.         
		 */
		Map<String, List<ElementCompletion>> grouped = new LinkedHashMap<>();
		for (ElementCompletion completion: suggest(rule, inputStatus)) {
			/*
			 *  key will be the new input (without considering mandatories of course), and we use 
			 *  this to group suggestions to facilitate mandatories calculation and exclusion 
			 *  logic 
			 */
			String key = inputContent.substring(0, completion.getReplaceBegin()) 
					+ completion.getReplaceContent() + inputContent.substring(completion.getReplaceEnd());
			List<ElementCompletion> value = grouped.get(key);
			if (value == null) {
				value = new ArrayList<>();
				grouped.put(key, value);
			}
			value.add(completion);
		}
		
		for (Map.Entry<String, List<ElementCompletion>> entry: grouped.entrySet())	 {
			List<ElementCompletion> value = entry.getValue();
			ElementCompletion completion = value.get(0);
			String description = completion.getDescription();
			String content = entry.getKey(); 
			
			/*
			 * if spec of suggested text matches current input around caret, we 
			 * will replace the match (instead of simply insert the suggested 
			 * text), and in this case, we do not need to append mandatories as
			 * most probably those mandatories already exist in current input
			 */
			if (completion.getReplaceEnd() <= inputStatus.getCaret()) {
				List<String> contents = new ArrayList<>();
				for (ElementCompletion each: value) {
					content = inputContent.substring(0, each.getReplaceBegin()) + each.getReplaceContent();
					ParentedElement parentElement = each.getExpectingElement().getParent();
					ElementSpec elementSpec = each.getExpectingElement().getSpec();
					for (String mandatory: getMandatoriesAfter(parentElement, elementSpec)) {
						String prevContent = content;
						content += mandatory;

						if (grammar.getTokenTypeByLiteral(mandatory) != null) {
							/*
							 * if mandatory can be appended without space, the last token 
							 * position should remain unchanged in concatenated content
							 */
							List<Token> tokens = grammar.lex(content);
							if (!tokens.isEmpty()) {
								Token lastToken = tokens.get(tokens.size()-1);
								if (lastToken.getStartIndex() != content.length() - mandatory.length()
										|| lastToken.getStopIndex() != content.length()-1) {
									content = prevContent + " " + mandatory;
								}
							} else {
								content = prevContent + " " + mandatory;
							}
						}
					}
					content += inputContent.substring(each.getReplaceEnd());
					
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
			if (completion.getCaret() == completion.getReplaceContent().length()) {
				// caret is not at the middle of replacement, so we need to move it to 
				// be after mandatory tokens
				caret = content.length() - inputContent.length() + completion.getReplaceEnd(); 
				if (content.equals(entry.getKey())) { 
					// in case mandatory tokens are not added after replacement, 
					// we skip existing mandatory tokens in current input
					String contentAfterCaret = content.substring(caret);
					ParentedElement parentElement = completion.getExpectingElement().getParent();
					ElementSpec elementSpec = completion.getExpectingElement().getSpec();
					caret += skipMandatories(contentAfterCaret, getMandatoriesAfter(parentElement, elementSpec));
				}
			} else {
				caret = completion.getReplaceBegin() + completion.getReplaceContent().length();
			}
			
			String replaceContent = content.substring(completion.getReplaceBegin(), 
					content.length()-inputContent.length()+completion.getReplaceEnd());
			inputSuggestions.add(new InputCompletion(completion.getReplaceBegin(), 
					completion.getReplaceEnd(), replaceContent, caret, description));
		}
		
		/*
		 * remove duplicate suggestions and suggestions the same as current input
		 */
		Set<String> suggestedContents = Sets.newHashSet(inputStatus.getContent());
		for (Iterator<InputCompletion> it = inputSuggestions.iterator(); it.hasNext();) {
			String content = it.next().complete(inputStatus).getContent();
			if (suggestedContents.contains(content))
				it.remove();
			else
				suggestedContents.add(content);
		}
		return inputSuggestions;
	}
	
	private ParentedElement getElementExpectingTerminal(ParentedElement parent, Element element) {
		parent = new ParentedElement(parent, element);
		Node node = element.getNode();
		if (node != null) { 
			element = node.getElements().get(node.getElements().size()-1);
			return getElementExpectingTerminal(parent, element);
		} else {
			return parent;
		}
	}
	
	private List<Element> assumeCompleted(EarleyParser parser, Node node, int endTokenIndex) {
		List<Element> rootElements = new ArrayList<>();
		if (node.getBeginTokenIndex() == 0 && node.getRuleSpec().getName().equals(parser.getRule().getName())) {
			rootElements.add(new Element(parser, null, endTokenIndex, node));
		} else {
			State startState = parser.getStates().get(node.getBeginTokenIndex());
			for (Node startNode: startState.getNodes()) {
				if (!startNode.isCompleted()) {
					ElementSpec nextElement = startNode.getExpectedElementSpec();
					if (nextElement instanceof RuleRefElementSpec) {
						RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
						if (ruleRefElement.getRuleName().equals(node.getRuleSpec().getName())) {
							List<Element> elements = new ArrayList<>(startNode.getElements());
							elements.add(new Element(parser, ruleRefElement, endTokenIndex, node));
							Node parentNode = new Node(startNode.getBeginTokenIndex(), 
									startNode.getRuleSpec(), startNode.getAlternativeSpecIndex(), 
									startNode.getExpectedElementSpecIndex(), 
									startNode.isExpectedElementSpecMatchedOnce(), 
									elements);
							rootElements.addAll(assumeCompleted(parser, parentNode, endTokenIndex));
						}
					}
				}
			}
		} 
		return rootElements;
	}
	
	private List<ElementSuggestion> suggest(EarleyParser parser, State state, InputStatus inputStatus) {
		List<ElementSuggestion> suggestions = new ArrayList<>();
		for (Node node: state.getNodesExpectingTerminal()) {
			List<Element> rootElements = assumeCompleted(parser, node, state.getEndTokenIndex());
			String matchWith;
			if (state.getEndTokenIndex() != 0) {
				int stopIndex = parser.getTokens().get(state.getEndTokenIndex()-1).getStopIndex()+1;
				matchWith = inputStatus.getContent().substring(stopIndex, inputStatus.getCaret());
			} else {
				matchWith = inputStatus.getContentBeforeCaret();
			}
			matchWith = StringUtils.trimStart(matchWith);
			for (Element rootElement: rootElements) {
				List<ParentedElement> expectingElements = new ArrayList<>();
				ParentedElement elementExpectingTerminal = getElementExpectingTerminal(null, rootElement);
				expectingElements.add(elementExpectingTerminal);
				ParentedElement expectingElement = elementExpectingTerminal.getParent();
				while (expectingElement != null) {
					if (expectingElement.getNode().getElements().size() == 1) {
						expectingElements.add(expectingElement);
						expectingElement = expectingElement.getParent();
					} else {
						break;
					}
				}
				
				Collections.reverse(expectingElements);
				
				List<InputSuggestion> inputSuggestions = null;
				for (ParentedElement element: expectingElements) {
					inputSuggestions = suggest(element, matchWith);
					if (inputSuggestions != null) {
						suggestions.add(new ElementSuggestion(element, matchWith, inputSuggestions));
						break;
					}
				}
				if (inputSuggestions == null) {
					TerminalElementSpec spec = (TerminalElementSpec) elementExpectingTerminal.getSpec();
					for (String leadingLiteral: spec.getLeadingLiterals(new HashSet<String>())) {
						List<Token> tokens = grammar.lex(leadingLiteral);
						if (tokens.size() == 1 && tokens.get(0).getType() == spec.getType()) {
							
						}
					}
				}
			}
		}
		return suggestions;
 	}
	
	private List<ElementCompletion> suggest(RuleSpec spec, InputStatus inputStatus) {
		List<ElementCompletion> completions = new ArrayList<>();
		
		List<ElementSuggestion> suggestions = new ArrayList<>();
		
		List<Token> tokens = grammar.lex(inputStatus.getContentBeforeCaret());
		EarleyParser parser = new EarleyParser(spec, tokens);
		
		if (parser.getStates().size() >= 1) {
			State lastState = parser.getStates().get(parser.getStates().size()-1);
			suggestions.addAll(suggest(parser, lastState, inputStatus));
		}

		/*
		 * do another match by not considering the last token. This is necessary for 
		 * instance for below cases:
		 * 1. when the last token matches either a keyword or part of an identifier. 
		 * For instance, the last token can be keyword 'for', but can also match 
		 * identifier 'forme' if the spec allows.  
		 * 2. assume we have a query rule containing multiple criterias, with each 
		 * criteria composed of key/value pair key:value. value needs to be quoted 
		 * if it contains spaces. In this case, we want to achieve the effect that 
		 * if user input value containing spaces without surrounding quotes, we 
		 * suggest the user to quote the value. 
		 */
		if (parser.getStates().size() >= 2) {
			State stateBeforeLast = parser.getStates().get(parser.getStates().size()-2);
			suggestions.addAll(suggest(parser, stateBeforeLast, inputStatus));
		}

		String inputContent = inputStatus.getContent();
		for (ElementSuggestion suggestion: suggestions) {
			int replaceStart = inputStatus.getCaret() - suggestion.getMatchWith().length();
			int replaceEnd = inputStatus.getCaret();
			String contentAfterReplaceStart = inputContent.substring(replaceStart);
			tokens = grammar.lex(contentAfterReplaceStart);
			
			/*
			 * if input around the caret matches spec of the suggestion, we then replace 
			 * the matched text with suggestion, instead of simply inserting the 
			 * suggested content
			 */
			if (!tokens.isEmpty() && tokens.get(0).getStartIndex() == 0) {
				ElementSpec elementSpec = (ElementSpec) suggestion.getExpectingElement().getSpec();
				int matchDistance = elementSpec.getEndOfMatch(tokens);
				if (matchDistance > 0) {
					int charIndex = tokens.get(matchDistance-1).getStopIndex()+1;
					if (replaceStart + charIndex > replaceEnd)
						replaceEnd = replaceStart + charIndex;
				}
			}

			String before = inputContent.substring(0, replaceStart);

			for (InputSuggestion inputSuggestion: suggestion.getInputSuggestions()) {
				int nextTokenIndex = suggestion.getExpectingElement().getEndTokenIndex();
				if (nextTokenIndex != 0) { 
					Token lastToken = parser.getTokens().get(nextTokenIndex-1);
					tokens = grammar.lex(before + inputSuggestion.getContent());
					/*
					 * ignore the suggestion if we can not append the suggested content directly. 
					 * This normally indicates that a space is required before the suggestion, 
					 * and we will show the suggestion when user presses the space to make the 
					 * suggestion list less confusing
					 */
					if (tokens.size() > nextTokenIndex) {
						Token newToken = tokens.get(nextTokenIndex-1);
						if (lastToken.getStartIndex() != newToken.getStartIndex()
								|| lastToken.getStopIndex() != newToken.getStopIndex()) {
							continue;
						}
					} else {
						continue;
					}
				} 
				
				completions.add(new ElementCompletion(suggestion.getExpectingElement(), replaceStart, 
						replaceEnd, inputSuggestion.getContent(), inputSuggestion.getCaret(), 
						inputSuggestion.getDescription()));
			}
		}
		return completions;
	}
	
	private int skipMandatories(String content, List<String> mandatories) {
		String mandatory = StringUtils.join(mandatories, "");
		mandatory = StringUtils.deleteWhitespace(mandatory);
		if (mandatory.length() != 0 && content.length() != 0) {
			int mandatoryIndex = 0;
			int contentIndex = 0;
			while (true) {
				char contentChar = content.charAt(contentIndex);
				/*
				 * space may exist between mandatories in user input, but should 
				 * not exist in ANTLR grammar  
				 */
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

	/*
	 * Get mandatory literals after specified node. For instance a method may have 
	 * below rule:
	 * methodName '(' argList ')'
	 * The mandatories after node methodName will be '('. When a method name is 
	 * suggested, we should add '(' and moves caret after '(' to avoid unnecessary
	 * key strokes
	 */
	private List<String> getMandatoriesAfter(ParentedElement parentElement, ElementSpec elementSpec) {
		List<String> literals = new ArrayList<>();
		if (parentElement != null && elementSpec != null 
				&& (elementSpec.getMultiplicity() == Multiplicity.ONE || elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_ONE)) {
			AlternativeSpec alternativeSpec = parentElement.getNode().getAlternativeSpec();
			int specIndex = alternativeSpec.getElements().indexOf(elementSpec);
			if (specIndex == alternativeSpec.getElements().size()-1) {
				elementSpec = parentElement.getSpec();
				parentElement = parentElement.getParent();
				return getMandatoriesAfter(parentElement, elementSpec);
			} else {
				elementSpec = alternativeSpec.getElements().get(specIndex+1);
				if (elementSpec.getMultiplicity() == Multiplicity.ONE
						|| elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
					MandatoryScan scan = elementSpec.scanMandatories(new HashSet<String>());
					literals = scan.getMandatories();
					if (!scan.isStop())
						literals.addAll(getMandatoriesAfter(parentElement, elementSpec));
				}
			}
		} 
		return literals;
	}

	public Grammar getGrammar() {
		return grammar;
	}

	protected abstract List<InputSuggestion> suggest(ParentedElement element, String matchWith);

}
