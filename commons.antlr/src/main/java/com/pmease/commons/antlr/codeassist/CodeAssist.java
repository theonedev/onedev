package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.pmease.commons.antlr.grammar.AlternativeSpec;
import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.Grammar;
import com.pmease.commons.antlr.grammar.RuleRefElementSpec;
import com.pmease.commons.antlr.grammar.RuleSpec;
import com.pmease.commons.antlr.grammar.TokenElementSpec;
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
		List<InputCompletion> inputCompletions = new ArrayList<>();
		
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
		for (ElementCompletion elementCompletion: suggest(rule, inputStatus)) {
			/*
			 *  key will be the new input (without considering mandatories of course), and we use 
			 *  this to group suggestions to facilitate mandatories calculation and exclusion 
			 *  logic 
			 */
			String key = inputContent.substring(0, elementCompletion.getReplaceBegin()) 
					+ elementCompletion.getReplaceContent() + inputContent.substring(elementCompletion.getReplaceEnd());
			List<ElementCompletion> value = grouped.get(key);
			if (value == null) {
				value = new ArrayList<>();
				grouped.put(key, value);
			}
			value.add(elementCompletion);
		}
		
		for (Map.Entry<String, List<ElementCompletion>> entry: grouped.entrySet())	 {
			List<ElementCompletion> value = entry.getValue();
			ElementCompletion completion = value.get(0);
			String description = completion.getDescription();
			String content = entry.getKey(); 
			
			/*
			 * only append mandatory literals if suggested content is complete and it does not 
			 * replace any part of current input. We do not append mandatories in the latter case
			 * as most probably they already exist in the current input
			 */
			if (completion.isComplete() && completion.getReplaceEnd() == inputStatus.getCaret()) {
				List<String> contents = new ArrayList<>();
				for (ElementCompletion each: value) {
					content = inputContent.substring(0, each.getReplaceBegin()) + each.getReplaceContent();
					ParentedElement parentElement = each.getExpectedElement().getParent();
					ElementSpec elementSpec = each.getExpectedElement().getSpec();
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
			 * Adjust caret to move it to next place expecting user input if suggestion is 
			 * complete, that is, we move it after all mandatory tokens after the replacement, 
			 * unless user puts caret explicitly in the middle of replacement via suggestion, 
			 * indicating the replacement has some place holders expecting user input. 
			 */
			int caret = completion.getCaret();
			if (completion.isComplete() && caret == completion.getReplaceContent().length()) {
				// caret is not at the middle of replacement, so we need to move it to 
				// be after mandatory tokens if provided suggestion is complete
				caret = content.length() - inputContent.length() + completion.getReplaceEnd(); 
				if (content.equals(entry.getKey())) { 
					// in case mandatory tokens are not added after replacement, 
					// we skip existing mandatory tokens in current input
					String contentAfterCaret = content.substring(caret);
					ParentedElement parentElement = completion.getExpectedElement().getParent();
					ElementSpec elementSpec = completion.getExpectedElement().getSpec();
					caret += skipMandatories(contentAfterCaret, getMandatoriesAfter(parentElement, elementSpec));
				}
			} else {
				caret += completion.getReplaceBegin();
			}
			
			String replaceContent = content.substring(completion.getReplaceBegin(), 
					content.length()-inputContent.length()+completion.getReplaceEnd());
			inputCompletions.add(new InputCompletion(completion.getReplaceBegin(), 
					completion.getReplaceEnd(), replaceContent, caret, description));
		}
		
		/*
		 * remove duplicate suggestions and suggestions the same as current input
		 */
		Set<String> suggestedContents = Sets.newHashSet(inputStatus.getContent());
		for (Iterator<InputCompletion> it = inputCompletions.iterator(); it.hasNext();) {
			String content = it.next().complete(inputStatus).getContent();
			if (suggestedContents.contains(content))
				it.remove();
			else
				suggestedContents.add(content);
		}
		return inputCompletions;
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
			int endTokenIndex = state.getEndTokenIndex();
			List<Element> elements = new ArrayList<>(node.getElements());
			elements.add(new Element(parser, node.getExpectedElementSpec(), 0, null));
			node = new Node(node.getBeginTokenIndex(), node.getRuleSpec(), 
					node.getAlternativeSpecIndex(), node.getExpectedElementSpecIndex(), 
					node.isExpectedElementSpecMatchedOnce(), elements);
			
			List<Element> rootElements = assumeCompleted(parser, node, endTokenIndex);
			String matchWith;
			Token token = state.getInitiatingToken();
			if (token != null) {
				int endCharIndex = token.getStopIndex()+1;
				matchWith = inputStatus.getContent().substring(endCharIndex, inputStatus.getCaret());
			} else {
				matchWith = inputStatus.getContentBeforeCaret();
			}
			matchWith = StringUtils.trimStart(matchWith);
			for (Element rootElement: rootElements) {
				List<ParentedElement> expectedElements = new ArrayList<>();
				ParentedElement elementExpectingTerminal = getElementExpectingTerminal(null, rootElement);
				expectedElements.add(elementExpectingTerminal);
				ParentedElement expectedElement = elementExpectingTerminal.getParent();
				while (expectedElement != null) {
					if (expectedElement.getNode().getElements().size() == 1) {
						expectedElements.add(expectedElement);
						expectedElement = expectedElement.getParent();
					} else {
						break;
					}
				}
				
				List<InputSuggestion> inputSuggestions = null;
				for (ParentedElement element: expectedElements) {
					inputSuggestions = suggest(element, matchWith);
					if (inputSuggestions != null) {
						if (!inputSuggestions.isEmpty())
							suggestions.add(new ElementSuggestion(element, matchWith, inputSuggestions));
						break;
					}
				}
				if (inputSuggestions == null && elementExpectingTerminal.getSpec() instanceof TokenElementSpec) {
					inputSuggestions = new ArrayList<>();
					TokenElementSpec spec = (TokenElementSpec) elementExpectingTerminal.getSpec();
					for (String leadingChoice: spec.getLeadingChoices()) {
						if (leadingChoice.startsWith(matchWith)) {
							List<Token> tokens = grammar.lex(leadingChoice);
							boolean complete = tokens.size() == 1 && tokens.get(0).getType() == spec.getTokenType(); 
							inputSuggestions.add(new InputSuggestion(leadingChoice, leadingChoice.length(), complete, null));
						}
					}
					if (!inputSuggestions.isEmpty())
						suggestions.add(new ElementSuggestion(elementExpectingTerminal, matchWith, inputSuggestions));
				}
			}
		}
		Collections.sort(suggestions, new Comparator<ElementSuggestion>() {

			@Override
			public int compare(ElementSuggestion suggestion1, ElementSuggestion suggestion2) {
				ParentedElement root1 = suggestion1.getExpectedElement().getRoot();
				ParentedElement root2 = suggestion2.getExpectedElement().getRoot();
				return compare(root1, root2);
			}
			
			private int compare(Element element1, Element element2) {
				Preconditions.checkState(element1.getNode() != null && element2.getNode() != null);
				int alternativeSpecIndex1 = element1.getNode().getAlternativeSpecIndex();
				int alternativeSpecIndex2 = element2.getNode().getAlternativeSpecIndex();
				if (alternativeSpecIndex1 != alternativeSpecIndex2)
					return alternativeSpecIndex1 - alternativeSpecIndex2;
				int expectedElementSpecIndex1 = element1.getNode().getExpectedElementSpecIndex();
				int expectedElementSpecIndex2 = element2.getNode().getExpectedElementSpecIndex();
				if (expectedElementSpecIndex1 != expectedElementSpecIndex2)
					return expectedElementSpecIndex1 - expectedElementSpecIndex2;
				element1 = element1.getNode().getElements().get(element1.getNode().getElements().size()-1);
				element2 = element2.getNode().getElements().get(element2.getNode().getElements().size()-1);
				if (element1.getNode() == null || element2.getNode() == null)
					return 0;
				else
					return compare(element1, element2);
			}
			
		});
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
			int replaceBegin = inputStatus.getCaret() - suggestion.getMatchWith().length();
			int replaceEnd = inputStatus.getCaret();

			/*
			 * if input around the caret matches spec of the suggestion, we then replace 
			 * the matched text with suggestion, instead of simply inserting the 
			 * suggested content
			 */
			tokens = grammar.lex(inputContent.substring(replaceBegin));
			if (!tokens.isEmpty() && tokens.get(0).getStartIndex() == 0) {   
				ElementSpec elementSpec = (ElementSpec) suggestion.getExpectedElement().getSpec();
				int endOfMatch = elementSpec.getEndOfMatch(tokens);
				if (endOfMatch > 0) { // there exist an element match
					int charIndex = replaceBegin + tokens.get(endOfMatch-1).getStopIndex()+1;
					if (charIndex > replaceEnd)
						replaceEnd = charIndex;
				}
			}

			String before = inputContent.substring(0, replaceBegin);

			for (InputSuggestion inputSuggestion: suggestion.getInputSuggestions()) {
				int endTokenIndex = suggestion.getExpectedElement().getRoot().getEndTokenIndex();
				if (endTokenIndex != 0) { 
					int lastMatchedTokenIndex = endTokenIndex-1;
					Token lastMatchedToken = parser.getTokens().get(lastMatchedTokenIndex);
					tokens = grammar.lex(before + inputSuggestion.getContent());
					
					/*
					 * ignore the suggestion if we can not append the suggested content directly. 
					 * This normally indicates that a space is required before the suggestion, 
					 * and we will show the suggestion when user presses the space to make the 
					 * suggestion list less confusing
					 */
					if (tokens.size() > lastMatchedTokenIndex) {
						Token newToken = tokens.get(lastMatchedTokenIndex);
						if (lastMatchedToken.getStartIndex() != newToken.getStartIndex()
								|| lastMatchedToken.getStopIndex() != newToken.getStopIndex()) {
							continue;
						}
					} else {
						continue;
					}
				} 
				
				completions.add(new ElementCompletion(suggestion.getExpectedElement(), replaceBegin,
						// replace only if provided suggestion is a complete representation of the element
						inputSuggestion.isComplete()?replaceEnd:inputStatus.getCaret(),  
						inputSuggestion.getContent(), inputSuggestion.getCaret(), 
						inputSuggestion.isComplete(), inputSuggestion.getDescription()));
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
		if (parentElement != null && elementSpec != null && !elementSpec.isMultiple()) {
			AlternativeSpec alternativeSpec = parentElement.getNode().getAlternativeSpec();
			int specIndex = alternativeSpec.getElements().indexOf(elementSpec);
			if (specIndex == alternativeSpec.getElements().size()-1) {
				elementSpec = parentElement.getSpec();
				parentElement = parentElement.getParent();
				return getMandatoriesAfter(parentElement, elementSpec);
			} else {
				elementSpec = alternativeSpec.getElements().get(specIndex+1);
				if (!elementSpec.isOptional()) {
					MandatoryScan scan = elementSpec.scanMandatories();
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

	/**
	 * Provide suggestions for expected element matching specified string 
	 * 
	 * @param expectedElement
	 * 			the element to expect. 
	 * @param matchWith
	 * 			the string the suggestion has to match with
	 * @return
	 * 			a list of suggestions. If you do not have any suggestions and want code assist to 
	 * 			drill down the element to provide default suggestions, return a <tt>null</tt> value 
	 * 			instead of an empty list
	 */
	@Nullable
	protected abstract List<InputSuggestion> suggest(ParentedElement expectedElement, String matchWith);

}
