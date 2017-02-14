package com.gitplex.commons.antlr.codeassist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import com.gitplex.commons.antlr.grammar.AlternativeSpec;
import com.gitplex.commons.antlr.grammar.ElementSpec;
import com.gitplex.commons.antlr.grammar.Grammar;
import com.gitplex.commons.antlr.grammar.RuleRefElementSpec;
import com.gitplex.commons.antlr.grammar.RuleSpec;
import com.gitplex.commons.antlr.grammar.TokenElementSpec;
import com.gitplex.commons.antlr.parser.Chart;
import com.gitplex.commons.antlr.parser.EarleyParser;
import com.gitplex.commons.antlr.parser.Element;
import com.gitplex.commons.antlr.parser.State;
import com.gitplex.commons.util.StringUtils;
import com.google.common.base.Preconditions;

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
	
	public List<String> getHints(InputStatus inputStatus, String ruleName) {
		RuleSpec rule = Preconditions.checkNotNull(grammar.getRule(ruleName));
		List<Token> tokens = grammar.lex(inputStatus.getContentBeforeCaret());
		EarleyParser parser = new EarleyParser(rule, tokens);
		
		Set<String> hints = new LinkedHashSet<>();
		for (Chart chart: getTerminatingCharts(parser)) {
			String matchWith = getMatchWith(chart, inputStatus);
			for (List<ParentedElement> expectChain: getExpectChains(chart)) {
				for (ParentedElement element: expectChain) 
					hints.addAll(getHints(element, matchWith));
			}
		}
		return new ArrayList<>(hints);
	}

	private String appendMandatories(InputStatus inputStatus, ElementCompletion completion) {
		String inputContent = inputStatus.getContent();
		String content = inputContent.substring(0, completion.getReplaceBegin()) 
				+ completion.getReplaceContent();
		if (completion.isComplete() && completion.getReplaceEnd() == inputStatus.getCaret()) {
			ParentedElement parentElement = completion.getExpectedElement().getParent();
			ElementSpec elementSpec = completion.getExpectedElement().getSpec();
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
		}
		return content + inputContent.substring(completion.getReplaceEnd());
	}
	
	public List<InputCompletion> suggest(InputStatus inputStatus, String ruleName, int count) {
		RuleSpec rule = Preconditions.checkNotNull(grammar.getRule(ruleName));
		
		List<InputCompletion> inputCompletions = new ArrayList<>();

		Map<String, List<ElementCompletion>> label2completions = new LinkedHashMap<>();
		for (ElementCompletion completion: suggest(rule, inputStatus, count)) {
			List<ElementCompletion> completionsWithLabel = label2completions.get(completion.getLabel());
			if (completionsWithLabel == null) {
				completionsWithLabel = new ArrayList<>();
				label2completions.put(completion.getLabel(), completionsWithLabel);
			}
			completionsWithLabel.add(completion);
		}
		String inputContent = inputStatus.getContent();
		for (Map.Entry<String, List<ElementCompletion>> entry: label2completions.entrySet()) {
			ElementCompletion completion = entry.getValue().get(0);
			String content = appendMandatories(inputStatus, completion);
			for (int i=1; i<entry.getValue().size(); i++) {
				String eachContent = appendMandatories(inputStatus, entry.getValue().get(i));
				if (!eachContent.equals(content)) { 
					// we have multiple mandatories attached to same label, so they are no longer 
					// mandatories any more
					content = completion.complete(inputStatus).getContent();
					break;
				}
			}
			
			/*
			 * Adjust caret to move it to next place expecting user input if suggestion is 
			 * complete, that is, we move it after all mandatory tokens after the replacement, 
			 * unless user puts caret explicitly in the middle of replacement via suggestion, 
			 * indicating the replacement has some place holders expecting user input. 
			 */
			int caret = completion.getCaret();
			if (completion.isComplete() && caret == -1) {
				// caret is not at the middle of replacement, so we need to move it to 
				// be after mandatory tokens if provided suggestion is complete
				caret = content.length() - inputContent.length() + completion.getReplaceEnd(); 
				if (content.equals(completion.complete(inputStatus).getContent())) { 
					// in case mandatory tokens are not added after replacement, 
					// we skip existing mandatory tokens in current input
					String contentAfterCaret = content.substring(caret);
					ParentedElement parentElement = completion.getExpectedElement().getParent();
					ElementSpec elementSpec = completion.getExpectedElement().getSpec();
					caret += skipMandatories(contentAfterCaret, getMandatoriesAfter(parentElement, elementSpec));
				}
			} else {
				if (caret == -1)
					caret = completion.getReplaceContent().length();
				caret += completion.getReplaceBegin();
			}
			
			String replaceContent = content.substring(completion.getReplaceBegin(), 
					content.length()-inputContent.length()+completion.getReplaceEnd());
			inputCompletions.add(new InputCompletion(completion.getReplaceBegin(), 
					completion.getReplaceEnd(), replaceContent, caret, completion.getLabel(), 
					completion.getDescription(), completion.getMatchRange()));
		}
		
		/*
		 * remove duplicate suggestions
		 */
		Set<String> suggestedContents = new HashSet<>();
		
		boolean hasNonWhitespaceSuggestions = false;
		for (Iterator<InputCompletion> it = inputCompletions.iterator(); it.hasNext();) {
			InputCompletion completion = it.next();
			if (!StringUtils.isWhitespace(completion.getReplaceContent()))
				hasNonWhitespaceSuggestions = true;
			String content = completion.complete(inputStatus).getContent();
			if (suggestedContents.contains(content))
				it.remove();
			else
				suggestedContents.add(content);
		}

		/*
		 * Preserve whitespace suggestions if there are no other suggestions as we may need to 
		 * insert a white space for other suggestions to appear
		 */
		if (hasNonWhitespaceSuggestions) {
			for (Iterator<InputCompletion> it = inputCompletions.iterator(); it.hasNext();) {
				if (it.next().getReplaceContent().trim().length() == 0)
					it.remove();
			}
		}
		
		if (inputCompletions.size() > count)
			return inputCompletions.subList(0, count);
		else
			return inputCompletions;
	}
	
	private ParentedElement getElementExpectingTerminal(ParentedElement parent, Element element) {
		parent = new ParentedElement(parent, element);
		State state = element.getState();
		if (state != null) { 
			// the last element is always the element assumed completed 
			element = state.getElements().get(state.getElements().size()-1);
			return getElementExpectingTerminal(parent, element);
		} else {
			return parent;
		}
	}
	
	private void assumeCompleted(EarleyParser parser, State state, int position, Set<State> statesAssumeCompleted) {
		Chart startChart = parser.getCharts().get(state.getOriginPosition());
		for (State startState: startChart.getStates()) {
			if (!startState.isCompleted()) {
				ElementSpec nextElement = startState.getExpectedElementSpec();
				if (nextElement instanceof RuleRefElementSpec) {
					RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
					if (ruleRefElement.getRuleName().equals(state.getRuleSpec().getName())) {
						List<Element> elements = new ArrayList<>(startState.getElements());
						elements.add(new Element(parser, ruleRefElement, position, state));
						State parentState = new State(startState.getOriginPosition(), 
								startState.getRuleSpec(), startState.getAlternativeSpecIndex(), 
								startState.getExpectedElementSpecIndex(), 
								startState.isExpectedElementSpecMatchedOnce(), 
								elements);
						if (statesAssumeCompleted.add(parentState))
							assumeCompleted(parser, parentState, position, statesAssumeCompleted);
					}
				}
			}
		}
	}

	private List<List<ParentedElement>> getExpectChains(Chart chart) {
		int position = chart.getPosition();
		EarleyParser parser = chart.getParser();
		List<List<ParentedElement>> expectChains = new ArrayList<>();
		for (State stateExpectingTerminal: chart.getStatesExpectingTerminal()) {
			List<Element> elements = new ArrayList<>(stateExpectingTerminal.getElements());
			elements.add(new Element(parser, stateExpectingTerminal.getExpectedElementSpec(), 0, null));
			stateExpectingTerminal = new State(stateExpectingTerminal.getOriginPosition(), stateExpectingTerminal.getRuleSpec(), 
					stateExpectingTerminal.getAlternativeSpecIndex(), stateExpectingTerminal.getExpectedElementSpecIndex(), 
					stateExpectingTerminal.isExpectedElementSpecMatchedOnce(), elements);
			
			/*
			 * Calculate all states assuming completed, this way we can get to the root state to 
			 * construct a partial parse tree to provide suggestion contexts
			 */
			Set<State> statesAssumeCompleted = new HashSet<>();
			if (statesAssumeCompleted.add(stateExpectingTerminal))
				assumeCompleted(parser, stateExpectingTerminal, position, statesAssumeCompleted);
			
			for (State stateAssumeCompleted: statesAssumeCompleted) {
				if (stateAssumeCompleted.getRuleSpec().getName().equals(parser.getRule().getName()) 
						&& stateAssumeCompleted.getOriginPosition() == 0) {
					Element rootElement = new Element(parser, null, position, stateAssumeCompleted);
					List<ParentedElement> expectChain = new ArrayList<>();
					
					ParentedElement elementExpectingTerminal = getElementExpectingTerminal(null, rootElement);
					expectChain.add(elementExpectingTerminal);
					
					/*
					 * Find out all parent elements not containing any matched tokens, and we will 
					 * ask if there are suggestions for these parent elements later. For instance, 
					 * if we input "Math.", we can provide suggestion for whole method element like 
					 * "max(a, b)"; otherwise, we can only provide suggestion for method name "max".  
					 */
					ParentedElement expectedElement = elementExpectingTerminal.getParent();
					while (expectedElement != null) {
						if (expectedElement.getState().getElements().size() == 1) {
							expectChain.add(expectedElement);
							expectedElement = expectedElement.getParent();
						} else {
							break;
						}
					}
					
					expectChains.add(expectChain);
				}
			}
		}
		return expectChains;
	}
	
	private String getMatchWith(Chart chart, InputStatus inputStatus) {
		String matchWith;
		Token token = chart.getOriginatingToken();
		int endCharIndex;
		if (token != null) {
			endCharIndex = token.getStopIndex()+1;
		} else {
			endCharIndex = 0;
		}
		matchWith = inputStatus.getContent().substring(endCharIndex, inputStatus.getCaret());
		for (Token each: chart.getParser().getTokens()) {
			if (each.getStartIndex() == endCharIndex) {
				// we can not trim leading space as it is not optional
				return matchWith;
			}
		}
		return StringUtils.trimStart(matchWith);
	}
	
	private List<ElementSuggestion> suggest(Chart chart, InputStatus inputStatus, int count) {
		List<ElementSuggestion> suggestions = new ArrayList<>();
		String matchWith = getMatchWith(chart, inputStatus);
		
		int numSuggested = 0;
		for (List<ParentedElement> expectChain: getExpectChains(chart)) {
			// goes through the expect chain to check if there are suggestions for expected element 
			List<InputSuggestion> inputSuggestions = null;
			for (ParentedElement element: expectChain) {
				inputSuggestions = suggest(element, matchWith, count - numSuggested);
				if (inputSuggestions != null) {
					if (!inputSuggestions.isEmpty()) {
						suggestions.add(new ElementSuggestion(element, matchWith, inputSuggestions));
						numSuggested += inputSuggestions.size();
					}
					break;
				}
			}
			
			ParentedElement elementExpectingTerminal = expectChain.get(0);
			if (inputSuggestions == null && elementExpectingTerminal.getSpec() instanceof TokenElementSpec) {
				// no suggestions, let's see if we can provide some default suggestions 
				inputSuggestions = new ArrayList<>();
				TokenElementSpec spec = (TokenElementSpec) elementExpectingTerminal.getSpec();
				for (String leadingLiteral: spec.getPossiblePrefixes()) {
					if (leadingLiteral.startsWith(matchWith) && leadingLiteral.length()>matchWith.length()) {
						List<Token> tokens = grammar.lex(leadingLiteral);
						boolean complete = tokens.size() == 1 && tokens.get(0).getType() == spec.getTokenType(); 
						InputSuggestion suggestion = wrapAsSuggestion(elementExpectingTerminal, leadingLiteral, complete);
						if (suggestion != null) {
							inputSuggestions.add(suggestion);
							if (++numSuggested >= count)
								break;
						}
					}
				}
				if (!inputSuggestions.isEmpty())
					suggestions.add(new ElementSuggestion(elementExpectingTerminal, matchWith, inputSuggestions));
			}
			
			if (numSuggested >= count)
				break;
		}
		
		return suggestions;
 	}
	
	private List<Chart> getTerminatingCharts(EarleyParser parser) {
		List<Chart> terminatingCharts = new ArrayList<>();
		
		/*
		 * Use the second last chart in order to not considering the last matched token. 
		 * This is useful for below cases:
		 * 1. when the last token matches either a keyword or part of an identifier. 
		 * For instance, the last token can be keyword 'for', but can also match 
		 * identifier 'forme' if the spec allows.  
		 * 2. assume we have a query rule containing multiple criterias, with each 
		 * criteria composed of key/value pair key:value. value needs to be quoted 
		 * if it contains spaces. In this case, we want to achieve the effect that 
		 * if user input value containing spaces without surrounding quotes, we 
		 * suggest the user to quote the value. 
		 */
		if (parser.getCharts().size() >= 2) 
			terminatingCharts.add(parser.getCharts().get(parser.getCharts().size()-2));
		
		if (parser.getCharts().size() >= 1)
			terminatingCharts.add(parser.getCharts().get(parser.getCharts().size()-1));

		return terminatingCharts;
	}
	
	private List<ElementCompletion> suggest(RuleSpec spec, InputStatus inputStatus, int count) {
		List<ElementCompletion> completions = new ArrayList<>();
		
		List<ElementSuggestion> suggestions = new ArrayList<>();

		List<Token> tokens = grammar.lex(inputStatus.getContentBeforeCaret());
		EarleyParser parser = new EarleyParser(spec, tokens);
		
		int numSuggested = 0;
		for (Chart chart: getTerminatingCharts(parser)) {
			List<ElementSuggestion> chartSuggestions = suggest(chart, inputStatus, count-numSuggested);
			suggestions.addAll(chartSuggestions);
			for (ElementSuggestion each: chartSuggestions)
				numSuggested += each.getInputSuggestions().size();
			if (numSuggested >= count)
				break;
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
			ElementSpec elementSpec = suggestion.getExpectedElement().getSpec();
			String contentAfterReplaceBegin = inputContent.substring(replaceBegin);

			int endOfMatch = getEndOfMatch(elementSpec, contentAfterReplaceBegin) + replaceBegin;
			tokens = grammar.lex(contentAfterReplaceBegin);
			if (endOfMatch > replaceEnd)
				replaceEnd = endOfMatch; 
			
			String before = inputContent.substring(0, replaceBegin);

			for (InputSuggestion inputSuggestion: suggestion.getInputSuggestions()) {
				int position = suggestion.getExpectedElement().getRoot().getPosition();
				if (position != 0) { 
					int lastMatchedTokenIndex = position-1;
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
						inputSuggestion.isComplete(), inputSuggestion.getLabel(), 
						inputSuggestion.getDescription(), inputSuggestion.getMatchRange()));
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
	 * Get mandatory literals after specified element. For instance a method may have 
	 * below rule:
	 * methodName '(' argList ')'
	 * The mandatories after element methodName will be '('. When a method name is 
	 * suggested, we should add '(' and moves caret after '(' to avoid unnecessary
	 * key strokes
	 */
	private List<String> getMandatoriesAfter(ParentedElement parentElement, ElementSpec elementSpec) {
		List<String> literals = new ArrayList<>();
		if (parentElement != null && elementSpec != null && !elementSpec.isMultiple()) {
			AlternativeSpec alternativeSpec = parentElement.getState().getAlternativeSpec();
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
	 * @param count
	 * 			maximum number of suggestions to return
	 * @return
	 * 			a list of suggestions. If you do not have any suggestions and want code assist to 
	 * 			drill down the element to provide default suggestions, return a <tt>null</tt> value 
	 * 			instead of an empty list
	 */
	@Nullable
	protected abstract List<InputSuggestion> suggest(ParentedElement expectedElement, String matchWith, int count);
	
	protected List<String> getHints(ParentedElement expectedElement, String matchWith) {
		return new ArrayList<>();
	}

	/**
	 * Get index of next character after the match. 
	 * 
	 * @param spec
	 * 			spec to be matched
	 * @param content
	 * 			content to match against
	 * @return
	 * 			index of next character after the match, or 0 if the rule 
	 * 			does not match any part of the string
	 */
	protected int getEndOfMatch(ElementSpec spec, String content) {
		int endOfMatch = 0;
		List<Token> tokens = grammar.lex(content);
		if (!tokens.isEmpty() && tokens.get(0).getStartIndex() == 0) {   
			endOfMatch = spec.getEndOfMatch(tokens);
			if (endOfMatch > 0) // there exist an element match
				endOfMatch = tokens.get(endOfMatch-1).getStopIndex()+1; // convert to char index
		}
		return endOfMatch;
	}
	
	/**
	 * Wrap specified literal of specified terminal element as suggestion.
	 * 
	 * @param expectedElement
	 * 			terminal element expecting to be matched next
	 * @param suggestedLiteral
	 * 			a proposed literal of above terminal element defined in grammar
	 * @param complete
	 * 			whether or not above literal is a complete representation of the 
	 * 			expected terminal element
	 * @return
	 * 			suggestion of the literal, or <tt>null</tt> to suppress this suggestion
	 */
	@Nullable
	protected InputSuggestion wrapAsSuggestion(ParentedElement expectedElement, 
			String suggestedLiteral, boolean complete) {
		if (StringUtils.isNotBlank(suggestedLiteral))
			return new InputSuggestion(suggestedLiteral, -1, complete, null, null);
		else
			return new InputSuggestion(suggestedLiteral, -1, complete, "space", null);
	}
	
}
